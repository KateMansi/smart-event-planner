# Smart Event Planner

A smart assistant that turns a few details about an event into a budget, a checklist, warnings and an AI-written summary. Built with Java 17 and Spring Boot, with the Gemini API for the summary.

**Live demo:** ADD-YOUR-CLOUD-RUN-LINK-HERE

## Chosen Vertical
Celebration and Event Planner.

## Problem and Solution
Planning a wedding, birthday, corporate event or party means juggling budget, timeline, food preferences and vendors at the same time. Most people do not know how to split a budget or what to book first.

This assistant takes the user's context (event type, date, budget, number of guests, city, food preference, indoor or outdoor, language) and produces a plan that changes with that context. A low budget, a close date or a large guest list each change the plan in a different way.

## Approach and Logic
Decisions are made by explicit, testable rules in `PlannerService`. Gemini is used only to write a friendly summary, so the plan stays correct even if the AI is unavailable.

| Situation | Decision |
|---|---|
| Event is less than 14 days away | URGENT mode. Venue and caterer booking moves to the top of the checklist, with a warning. |
| Budget per guest is below Rs 500 | 10 percentage points of the budget move from Decor to Catering, with a warning. |
| More than 200 guests | Parking, crowd control and security tasks are added. |
| Outdoor event in June to September | A covered backup venue is added, with a monsoon warning. |
| Food preference (Veg, Non-veg, Jain, Halal) | The matching catering check is added to the checklist. |
| Event type (Wedding, Birthday, Corporate, Party) | A different budget split and extra checklist items for that type. |

A contingency line absorbs rounding, so the budget breakdown always adds up to the total.

## How It Works
1. The browser form sends JSON to `POST /api/plan`.
2. A rate limit filter allows at most 20 API requests per client per minute.
3. Bean Validation checks every field on the server.
4. `PlannerService` applies the rules above and builds the budget, checklist and warnings.
5. `GeminiClient` asks Gemini for a short summary in the chosen language (English, Hindi or Marathi). If the main model is busy, a backup model is tried. If both fail, the rule-based plan is still returned with a clear message.
6. Successful results are cached for one hour, so identical requests are served instantly and do not call Gemini again.

## Features
- Context-aware plan: budget breakdown, checklist, warnings and urgent mode
- Multilingual interface and AI summary: English, Hindi, Marathi
- Works without the AI: the rule-based plan is always returned
- Response caching, rate limiting and a backup AI model
- Accessible, responsive form with dark mode support

## Gen AI Usage
The Gemini API generates the plan summary and three practical tips in the language the user chose. The main model is `gemini-flash-latest`, with `gemini-3.1-flash-lite` as backup. The user's free-text notes are passed inside delimiters, with an instruction to treat them as data and never as instructions, which reduces prompt injection risk.

## Assumptions
- All amounts are in Indian rupees and dates use the India time zone.
- The monsoon is taken as June to September.
- Budget percentages are typical starting points, not vendor quotes.
- Rs 500 per guest is treated as a low budget.
- "Urgent" means fewer than 14 days to the event.

## Security
- The API key is read from the `GEMINI_API_KEY` environment variable and is never committed.
- Server-side validation with allow-listed values and length limits on every field.
- Content-Security-Policy and other security headers, with no inline scripts or styles.
- Rate limiting per client and a request size limit.
- Error responses never include stack traces.
- The frontend writes results with `textContent`, never `innerHTML`.
- The Docker image runs as a non-root user.

## Accessibility
- Semantic HTML, labelled inputs and a skip link
- Results announced to screen readers with `aria-live`, errors with `role="alert"`
- Full keyboard navigation with visible focus
- High contrast colours, dark mode and reduced-motion support
- Responsive layout for phones, tablets and desktops
- `lang` attribute changes with the selected language

## Efficiency
- Identical requests are served from an in-memory cache
- One Gemini call per new plan, with a timeout and a backup model
- Small Docker image using a JRE-only runtime stage
- Rate limiting protects both the server and the AI quota

## Testing
Run `mvn test` to execute 14 tests covering:
- Planner rules (urgent mode, low budget, large events, monsoon, diet, budget totals)
- Controller behaviour (valid request, invalid input, AI failure, security headers, caching)
- Rate limiting (blocking after the limit, other clients unaffected)

Gemini is mocked in tests, so no API key is needed to run them.

## Run Locally
Requirements: Java 17 and Maven.

    git clone https://github.com/KateMansi/smart-event-planner.git
    cd smart-event-planner
    mvn test

Start the app (the AI summary needs a Gemini API key; without one the rule-based plan still works):

    set GEMINI_API_KEY=your-key
    mvn spring-boot:run

On PowerShell use `$env:GEMINI_API_KEY="your-key"` and on Git Bash use `export GEMINI_API_KEY="your-key"`.

Open http://localhost:8080

## Deploy
The app is deployed on Google Cloud Run.

    gcloud run deploy smart-event-planner --source . --region asia-south1 --allow-unauthenticated --memory 512Mi
    gcloud run services update smart-event-planner --region asia-south1 --set-env-vars GEMINI_API_KEY=your-key

## Project Structure
    src/main/java/com/mansi/planner/
        model/      request and response records
        service/    planner rules, Gemini client, cache
        web/        controller, validation errors, security headers, rate limit
    src/main/resources/static/   accessible multilingual frontend
    src/test/java/com/mansi/planner/   unit and web tests