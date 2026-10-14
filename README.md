# VoyaAI

VoyaAI keeps its existing React, Vite, Spring Boot, and PostgreSQL project structure. Registration and login use BCrypt password hashes and signed JWTs; authentication is always sent to the real Spring Boot API, independently of the existing mock-data setting for trip features.

## Run locally

Requirements: Node.js, Java 21, PostgreSQL, and the Maven wrapper included in `backend/`.

1. Start PostgreSQL and create the `voyaai` database if it does not exist. Hibernate uses `ddl-auto=update` and applies compatible schema updates without dropping tables. Review existing `users` rows first: a pre-existing plaintext password cannot be used as a BCrypt hash; reset those passwords before allowing those accounts to log in.
2. Copy `backend/.env.example` to `backend/.env` and set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and a unique `JWT_SECRET`. Generate the key with `openssl rand -base64 32` or PowerShell: `[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))`. Do not commit either `.env` file or share the signing key. `JWT_EXPIRATION_MS` defaults to 86400000 (24 hours).
3. In `backend/`, run `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`).
4. In the project root, run `npm install` once, then `npm run dev`. The frontend defaults to `http://localhost:8080/api`; set `VITE_API_BASE_URL` in the root `.env` if your backend uses another address.

`GET /api/health` is public. Every other backend route requires a valid Bearer token except the registration and login routes. Existing trip mock-data behavior remains controlled by `VITE_USE_MOCK_DATA`.

## Authentication APIs

- `POST /api/auth/register` accepts `{ "name", "email", "password" }`, validates input, hashes the password, and returns a safe user profile with HTTP 201. Duplicate email returns 409.
- `POST /api/auth/login` accepts `{ "email", "password" }`, checks BCrypt, and returns `{ "token", "user" }`. Invalid credentials return 401.
- `GET /api/auth/me` requires `Authorization: Bearer <token>` and returns only `id`, `name`, `email`, and `createdAt`.

The browser stores the JWT in `localStorage` so a refresh can restore the session. Startup verifies it with `/api/auth/me`; expired or invalid tokens are removed. Protected React routes redirect to Login, and authenticated users visiting Login or Sign Up go to Dashboard. Logout removes the token and returns Home.

## Browser flow and edge cases

Open the Vite URL, select **Sign Up**, and create an account with a valid name, email, and password of at least 8 characters. Successful registration signs in and opens Dashboard. Sign out from the navbar, then sign in with that account. Refresh Dashboard to verify session restoration. Try the same email again (409), an invalid email/empty field/short password (validation message), and a wrong or unknown login (generic invalid-credentials message). While signed out, open `/dashboard` or `/profile` directly to verify the Login redirect. After logout, protected pages should redirect again. To check JWT rejection, replace the value of `voyaai_token` in browser developer tools with an invalid value and refresh a protected page; the token should be cleared and the page should require Login.

Registration and login have no mock-user fallback. Passwords are never returned from APIs. Only the JWT signing secret and database credentials belong in the ignored backend `.env`; do not put secrets in Vite variables.

## Trip Optimization

> **Important Note:** Optimization results are recommendations based on available trip data and ML-estimated costs. They are not guaranteed live booking prices.

### Purpose
The Trip Optimization engine takes an existing user trip and evaluates better, feasible configurations based on user constraints and preferences. It operates as a transparent scoring layer built on top of trip data, itinerary activity details, verified destination weather, and real ML cost predictions from the Python FastAPI regression service.

### Optimization Modes
- **BUDGET:** Minimizes estimated trip cost by selecting high-efficiency transit modes and smart accommodations while preserving core activities.
- **BALANCED:** Balances travel expenditure, convenient travel pace, verified sights, and interest matching.
- **EXPERIENCE:** Prioritizes comfort, verified attractions, and deep interest alignment while penalizing excessive cost spikes.

### Hard Constraints
The optimizer strictly enforces immutable trip requirements:
- **Destination:** Never altered (e.g. Goa remains Goa).
- **Traveler Count:** Never altered (e.g. 2 travelers remains 2 travelers).
- **Trip Type:** Preserved (`NATIONAL` vs `INTERNATIONAL`). International routes always preserve `Flight` transport.
- **Strict Budget:** When `strictBudget = true` or `maxBudget` is set, any candidate with `estimatedCost > maxBudget` is rejected. If no candidate fits, a clear explanation is returned without forcing invalid options.

### Soft Preferences
Adjusted according to the selected mode and user preferences:
- Accommodation tier (`Hostel`, `Guesthouse`, `Hotel`, `Resort`, `Luxury Hotel`)
- Transport mode (`Train`, `Car`, `Flight`)
- Activity scope and density
- User interest matching (`Food`, `Culture`, `Beach`, `Nature`, `Relaxation`, `Sightseeing`, `Nightlife`)

### Candidate Generation
Generates a controlled search space of 5–6 realistic, differentiated archetypes depending on national vs international corridors:
- **National:** Budget Saver (Train + Hostel), Value Route (Train + Guesthouse), Road Explorer (Car + Hotel), Balanced Express (Flight + Hotel), Leisure Comfort (Flight + Resort), Luxury Experience (Flight + 5-Star Hotel).
- **International:** Smart Explorer (Flight + Hostel), Value Voyager (Flight + Guesthouse), Balanced International (Flight + Hotel), Resort Retreat (Flight + Resort), Signature Luxury (Flight + 5-Star Hotel).

### ML Integration
- Reuses the Python FastAPI ML service (`/predict` endpoint).
- Predicts baseline trip cost using the base plan configuration.
- Predicts each candidate's total trip cost using the trained scikit-learn regression pipeline.
- Calculates estimated savings as: `baselineCost - candidateEstimatedCost`.

### Scoring & Ranking
- Project-level, explainable scoring formula on a 0–100 scale:
  - **Cost Efficiency (`costEff`):** Normalized savings compared to baseline cost.
  - **Interest Match (`interestEff`):** Proportion of user interests genuinely covered by activities/focus.
  - **Comfort Rating (`comfortEff`):** Normalized index based on transport convenience and accommodation tier.
  - Weights vary by mode:
    - `BUDGET`: 60% Cost + 25% Interest + 15% Comfort
    - `BALANCED`: 35% Cost + 35% Interest + 30% Comfort
    - `EXPERIENCE`: 15% Cost + 40% Interest + 45% Comfort
- Candidates are sorted descending by score; top 3 diverse candidates are returned.

### API Endpoint
```http
POST /api/trips/{tripId}/optimize
```
Requires JWT Authentication (`Authorization: Bearer <token>`) and verifies trip ownership.

#### Request Example
```json
{
  "mode": "BUDGET",
  "maxBudget": 60000.0,
  "strictBudget": false,
  "preserveTransport": false,
  "preserveAccommodation": false
}
```

#### Response Example
```json
{
  "tripId": 70,
  "destination": "Goa",
  "tripType": "NATIONAL",
  "mode": "BUDGET",
  "baselineCost": 61548.96,
  "currency": "INR",
  "weatherSummary": "Overcast, 24°C",
  "results": [
    {
      "rank": 1,
      "title": "Budget Saver (Scenic Rail & Hostel)",
      "mode": "BUDGET",
      "transport": "Train",
      "accommodation": "Hostel",
      "activityCount": 8,
      "activityFocus": "Self-guided walks, scenic vistas, and authentic street food",
      "estimatedCost": 56459.93,
      "currency": "INR",
      "estimatedSavings": 5089.03,
      "interestMatchScore": 1.0,
      "matchedInterests": ["Beach", "Food", "Culture"],
      "comfortScore": 0.45,
      "optimizationScore": 68.0,
      "highlights": [
        "Economical rail transport significantly lowers intercity travel spend",
        "Hostel accommodation keeps nightly lodging costs minimal",
        "Curated focus on public beaches, historic streets, and authentic local food"
      ],
      "costDifferenceMessage": "Estimated savings of ₹5089 compared to base plan"
    }
  ],
  "message": "Found 3 optimized trip configurations in BUDGET mode.",
  "disclaimer": "Optimization results are recommendations based on available trip data and ML-estimated costs. They are not guaranteed live booking prices."
}
```

### Known Limitations
- Costs are statistical estimates from the trained ML regression pipeline, not live real-time booking prices from airline or hotel GDS engines.
- Weather awareness uses current and forecast conditions from the external weather integration; the optimizer does not claim to predict long-term seasonal anomalies.

