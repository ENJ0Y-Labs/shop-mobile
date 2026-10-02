# AGENT.md

# HNG15 Lesson 3 - Android Shop App

## Project Identity

* Project: ENJ0Y Solution Android Shop App
* Repository: Android mobile application for the existing `shop-website`
* HNG Stage: HNG15 Lesson 3
* Platform: Android
* Language: Kotlin
* UI: Jetpack Compose
* Backend: Existing Flask REST API from `ENJ0Y-Labs/shop-website`
* Purpose: Provide a mobile shopping experience using the exact same backend API and customer account system as the existing web application.

The Android app is a client of the existing shop backend.

Do not create a second backend.

Do not create a second database.

Do not duplicate business logic that already belongs on the server.

---

# 1. Core HNG15 Requirement

The application must satisfy the Lesson 3 requirement:

> Make a mobile app for the shop website and use the same API endpoints.

A user must be able to:

1. Log in on the website.
2. Log in on the Android app using the same account.
3. Browse products on mobile.
4. Add products to the cart.
5. View the same cart on the website and mobile app.
6. Modify cart quantities.
7. Remove cart items.
8. Clear the cart.
9. Continue to use the existing shop backend.
10. Test the application on a physical Android phone.

Most importantly:

```text
WEB
Add product to cart
        ↓
Existing Flask API
        ↓
PostgreSQL
        ↓
MOBILE
GET /api/cart
        ↓
Same cart appears
```

The Android app must not maintain an independent authenticated-user cart that becomes the source of truth.

The backend database is the source of truth for authenticated users.

---

# 2. Existing Backend Must Be Reused

The existing shop backend is already implemented in:

```text
shop-website/
└── backend/
    └── app/
        ├── models/
        ├── routes/
        ├── services/
        └── config/
```

The backend uses:

* Python
* Flask
* SQLAlchemy
* PostgreSQL
* Flask-Session
* Server-side sessions
* Google OAuth
* Mailgun
* Supabase PostgreSQL

The Android app must consume this API.

Do not rewrite the backend merely to make Android development easier.

Do not introduce a mobile-specific API unless the existing API genuinely cannot support the HNG requirement.

---

# 3. Existing API Base

The existing API uses:

```text
/api/
```

Development default:

```text
http://localhost:5000/api
```

Production API URL must be supplied through Android configuration.

Never hardcode a production backend URL throughout the application.

Use one centralized API configuration.

Example:

```text
BuildConfig.API_BASE_URL
```

or an equivalent centralized configuration mechanism.

---

# 4. Existing Authentication

The existing backend uses:

```text
Server-side sessions
```

The session cookie is:

```text
enj0y_session
```

The backend stores the authenticated user's ID in the server-side session.

Authentication is therefore NOT JWT-based.

Do not invent JWT authentication for the Android application.

Do not modify the backend authentication system just because JWT might be easier to use from Android.

The Android HTTP client must preserve the session cookie and send it on subsequent requests.

---

# 5. Authentication Endpoints

Existing endpoints:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
GET  /api/auth/me
GET  /api/auth/google
GET  /api/auth/google/callback
```

## Login

Request:

```json
{
  "email": "user@example.com",
  "password": "password"
}
```

Endpoint:

```text
POST /api/auth/login
```

Successful response contains:

```json
{
  "user": {
    "id": "...",
    "email": "...",
    "name": "...",
    "profile_picture_url": "..."
  }
}
```

The backend creates the authenticated server-side session.

The Android client must preserve the returned session cookie.

---

# 6. Current User

Use:

```text
GET /api/auth/me
```

This endpoint determines whether the Android client currently has an authenticated session.

On application startup:

```text
App starts
    ↓
GET /api/auth/me
    ↓
200 → user is authenticated
401 → user is not authenticated
```

Do not assume that a locally stored boolean such as:

```text
isLoggedIn = true
```

means the server session is still valid.

The server is authoritative.

---

# 7. Logout

Use:

```text
POST /api/auth/logout
```

The backend clears the server-side session.

The Android client must also clear its local session cookie/cache as appropriate.

---

# 8. Session Cookie Requirements

The web application uses:

```javascript
credentials: "include"
```

because authentication relies on cookies.

Android must provide equivalent behavior through its HTTP client.

Use:

* Retrofit
* OkHttp
* CookieJar

or an equivalent HTTP stack capable of persistent cookie handling.

Recommended approach:

```text
Retrofit
    ↓
OkHttp
    ↓
CookieJar
    ↓
enj0y_session
```

The cookie must survive between API calls during the authenticated session.

Do not manually copy session IDs into request bodies.

Do not expose the session cookie to the UI.

Do not log session cookies.

Do not store authentication secrets in plain text logs.

---

# 9. Critical Authentication Constraint

The Android app and web application must authenticate against the same backend.

Example:

```text
User
email: noble@example.com
```

Web:

```text
POST /api/auth/login
```

Mobile:

```text
POST /api/auth/login
```

Both authenticate against the same PostgreSQL-backed user account.

The Android application must not create a separate mobile account system.

---

# 10. Google OAuth

The existing website supports Google OAuth:

```text
GET /api/auth/google
GET /api/auth/google/callback
```

For the HNG mobile MVP, email/password authentication should be implemented first.

Do not create a custom Google OAuth implementation unless required.

If Google authentication is added to Android, it must be designed around the existing backend OAuth flow rather than creating a second user identity system.

Never expose:

```text
GOOGLE_CLIENT_SECRET
```

or any other server-side secret inside the Android application.

---

# 11. Products API

Existing endpoints:

```text
GET /api/products
GET /api/products/:id
```

The product listing supports:

```text
search
category
sort
page
per_page
```

Example:

```text
GET /api/products?page=1&per_page=12
```

Product search:

```text
GET /api/products?search=shirt
```

Category:

```text
GET /api/products?category=...
```

Sorting:

```text
GET /api/products?sort=name
GET /api/products?sort=price_asc
GET /api/products?sort=price_desc
```

The exact supported values must be confirmed from the existing backend before implementation.

Do not duplicate product data in the Android application.

The server remains the source of truth.

---

# 12. Product Model

The existing backend product model contains:

```text
id
name
description
price
image_url
category
stock
variants
created_at
updated_at
```

The Android model should represent the API response accurately.

Use Kotlin serialization or Moshi/Gson consistently.

Do not create unnecessary duplicate representations of the same product.

---

# 13. Money

The backend stores product prices as integer values.

Do not convert prices to Kotlin `Double` merely because it is convenient.

Money must not be handled using floating-point arithmetic.

Use an integer representation consistent with the API.

For example:

```text
150000
```

must remain an integer value when received from the API.

Formatting for display belongs in the UI/presentation layer.

The Android client must never calculate a final order price and send it as an authoritative value.

---

# 14. Cart API

The existing backend provides:

```text
GET    /api/cart
POST   /api/cart/items
PATCH  /api/cart/items/:id
DELETE /api/cart/items/:id
DELETE /api/cart
POST   /api/cart/merge
```

These endpoints already exist.

Use them.

Do not create:

```text
/api/mobile/cart
```

or:

```text
/api/android/cart
```

without an explicit backend requirement.

---

# 15. Get Cart

Use:

```text
GET /api/cart
```

Successful response:

```json
{
  "cart": {
    "id": "...",
    "items": [],
    "total": 0,
    "item_count": 0
  }
}
```

The Android application must load the authenticated user's cart from the backend.

---

# 16. Add Cart Item

Use:

```text
POST /api/cart/items
```

Request:

```json
{
  "product_id": "...",
  "quantity": 1
}
```

The backend validates:

* Product existence
* Quantity
* Stock
* Existing cart item
* Final quantity

Do not trust the Android client to determine whether a product is available.

---

# 17. Update Cart Item

Use:

```text
PATCH /api/cart/items/:id
```

Request:

```json
{
  "quantity": 2
}
```

The backend validates the quantity and stock.

The Android UI must update its displayed state from the successful API response.

---

# 18. Remove Cart Item

Use:

```text
DELETE /api/cart/items/:id
```

Do not merely hide the item locally and assume deletion succeeded.

Update local UI state after the server confirms the operation.

---

# 19. Clear Cart

Use:

```text
DELETE /api/cart
```

The server clears the authenticated user's cart.

---

# 20. Cart Synchronization

This is the most important Lesson 3 requirement.

The mobile application and website must use the same persistent cart.

Correct architecture:

```text
             PostgreSQL
                  ▲
                  │
             Flask API
             ▲       ▲
             │       │
          Website  Android
```

Incorrect architecture:

```text
Website → browser localStorage

Android → local Android storage

Backend → another cart

```

That creates three competing realities, which is exactly the sort of thing software engineers later describe as "an interesting bug."

For authenticated users:

```text
Backend cart = source of truth
```

---

# 21. Cross-Platform Cart Test

This test is mandatory.

### Test A: Website → Mobile

1. Log into the website.
2. Add Product A.
3. Confirm it appears in the website cart.
4. Open the Android application.
5. Log in using the same account.
6. Open the cart.
7. Confirm Product A appears.

Expected:

```text
Website cart
=
Mobile cart
```

### Test B: Mobile → Website

1. Log into Android.
2. Add Product B.
3. Confirm Product B appears in mobile cart.
4. Open the website.
5. Log into the same account.
6. Open the cart.
7. Confirm Product B appears.

### Test C: Quantity synchronization

Website:

```text
Product A
quantity = 1
```

Change on Android:

```text
quantity = 3
```

Reload/open the website cart.

Expected:

```text
quantity = 3
```

### Test D: Removal synchronization

Remove an item on Android.

Then open the website cart.

The item must also be absent.

---

# 22. Meaning of "Instantly"

The requirement says that adding something to the cart on web should make it instantly reflect in mobile.

The first implementation must ensure that the mobile cart always retrieves the current backend cart rather than relying on stale local state.

At minimum:

```text
Open Cart
    ↓
GET /api/cart
    ↓
Render latest server state
```

Also refresh the cart when the cart screen becomes active again.

Do not assume that an Android process remaining alive means its cart state is current.

If the backend does not provide WebSockets or Server-Sent Events, do not invent a fake real-time system.

For this stage, prioritize correct shared backend state and fresh cart retrieval.

---

# 23. Local Visitor Cart

The existing website supports an unauthenticated local cart.

The Android app may support a local visitor cart if needed.

However:

```text
Unauthenticated cart
```

and:

```text
Authenticated database cart
```

must remain conceptually separate.

When authentication occurs, follow the backend's existing merge behavior:

```text
POST /api/cart/merge
```

Do not invent a different cart-merging protocol.

---

# 24. Orders API

Existing endpoints:

```text
POST /api/orders
GET  /api/orders
GET  /api/orders/:id
```

Orders require authentication.

The Android app should only implement checkout/order functionality if it is required for the Lesson 3 submission or necessary to preserve the existing shop experience.

Do not expand the scope unnecessarily.

---

# 25. Android Architecture

Use a simple architecture:

```text
UI
 ↓
ViewModel
 ↓
Repository
 ↓
Retrofit API
 ↓
OkHttp
 ↓
Flask API
 ↓
PostgreSQL
```

Recommended structure:

```text
app/
├── data/
│   ├── api/
│   ├── model/
│   ├── repository/
│   └── session/
│
├── ui/
│   ├── components/
│   ├── screens/
│   └── theme/
│
├── viewmodel/
│
└── MainActivity.kt
```

Do not create a giant architecture for a small HNG application.

The purpose of architecture is to make changes easier, not to give the folder tree emotional depth.

---

# 26. Recommended Android Technologies

Use:

```text
Kotlin
Jetpack Compose
AndroidX
ViewModel
Coroutines
Retrofit
OkHttp
Kotlin Serialization
```

Prefer established Android libraries over custom networking code.

Do not introduce additional libraries unless there is a clear reason.

---

# 27. UI Requirements

The Android application should be:

* Clean
* Modern
* Responsive
* Easy to navigate
* Consistent with the existing shop website
* Designed specifically for mobile interaction

The application should not simply be the website squeezed into a phone-shaped rectangle.

Use native Android layouts and interaction patterns.

---

# 28. Required Screens

Minimum recommended screens:

```text
Shop/Home
    ↓
Product Details
    ↓
Cart

Login
Register

Checkout
Order Confirmation

Orders
Order Details
```

For the HNG requirement, authentication, product browsing, and cart synchronization are the critical paths.

---

# 29. Navigation

Use Jetpack Compose navigation or the current stable Android navigation solution selected for the project.

Keep navigation centralized.

Avoid passing large mutable objects between screens.

Prefer passing identifiers and loading authoritative data when necessary.

Example:

```text
ProductDetails(productId)
```

instead of:

```text
ProductDetails(entire mutable Product object)
```

---

# 30. UI State

Screens should explicitly represent:

```text
Loading
Success
Error
Empty
```

Example:

```text
Loading products
        ↓
Products loaded
        ↓
Display products
```

Network failure:

```text
API request
    ↓
Error
    ↓
User-friendly error message
```

Do not expose raw HTTP stack traces to users.

---

# 31. Network Error Handling

The API returns structured errors.

The Android application should extract useful messages from the response.

Common cases include:

```text
401
Authentication required
```

```text
404
Product/cart item not found
```

```text
409
Insufficient stock
```

```text
400
Invalid request
```

Do not display:

```text
HTTP 409 IOException Retrofit ...
```

as the primary user-facing message.

Humans have enough trouble buying things without debugging your HTTP client while doing it.

---

# 32. Authentication State

The application should have a centralized authentication state.

Example states:

```text
Unknown
Authenticated
Unauthenticated
```

Application startup:

```text
Unknown
   ↓
GET /api/auth/me
   ↓
Authenticated / Unauthenticated
```

Do not scatter authentication checks throughout individual composables.

---

# 33. Cart State

The cart should have centralized state.

Example:

```text
CartState
├── loading
├── cart
├── error
└── refreshing
```

Cart operations should go through a repository/ViewModel rather than having UI components directly perform HTTP requests.

---

# 34. Refresh Behavior

Refresh the cart when:

* The cart screen opens.
* The application returns to the foreground where appropriate.
* A cart mutation succeeds.
* Authentication changes.
* The user explicitly refreshes.

Do not continuously poll the backend every second.

That is not "real time." That is simply repeatedly asking the server if it has noticed your existence.

---

# 35. Offline Behavior

Offline support is not a primary HNG15 Lesson 3 requirement.

Do not build a complete offline-first architecture.

If the device loses internet access:

* Show a clear error.
* Preserve unsaved UI input where reasonable.
* Do not pretend that a cart mutation succeeded.
* Retry only when appropriate.

The backend remains authoritative.

---

# 36. API Models

Create Kotlin data classes that mirror the existing JSON contracts.

Examples:

```text
User
Product
ProductVariant
Cart
CartItem
Order
OrderItem
ApiError
```

Keep API models separate from UI state when the distinction becomes useful.

Do not blindly copy backend database models into Android.

The Android app consumes API representations, not PostgreSQL tables.

---

# 37. Repository Rules

Repositories should:

* Call the API.
* Handle API-level failures.
* Expose useful results to ViewModels.
* Avoid UI logic.
* Avoid Compose dependencies.

Example:

```text
AuthRepository
ProductRepository
CartRepository
OrderRepository
```

Only create repositories that are actually needed.

---

# 38. Retrofit Rules

Define API interfaces around the existing endpoints.

Example:

```text
AuthApi
ProductApi
CartApi
OrderApi
```

The endpoint paths must remain consistent with the backend.

Do not change:

```text
/api/cart
```

to:

```text
/cart
```

unless the centralized base URL already contains `/api`.

Keep URL composition consistent.

---

# 39. API Base URL

Development and production must be treated differently.

Android emulator:

```text
localhost
```

does NOT refer to the development computer.

For a backend running on the developer's PC, the Android emulator normally accesses the host through:

```text
10.0.2.2
```

A physical phone requires the computer's LAN IP when connecting to a locally running backend, provided the backend is reachable from the phone.

For production testing, use the deployed HTTPS Render backend URL.

Never hardcode a personal LAN IP into application logic.

---

# 40. HTTPS

Production API communication must use HTTPS.

Do not disable TLS certificate validation.

Do not add permissive trust managers merely to make development work.

Do not ship:

```text
cleartext HTTP
```

as the production configuration.

---

# 41. Secrets

Never store backend secrets inside the Android application.

Never include:

```text
DATABASE_URL
SECRET_KEY
GOOGLE_CLIENT_SECRET
MAILGUN_API_KEY
SUPABASE_SERVICE_ROLE_KEY
```

in the Android project.

The Android application is a client.

Anything shipped inside the APK should be considered discoverable.

---

# 42. Logging

Development logging may be used for:

* Request status
* Debugging state transitions
* Non-sensitive API responses

Never log:

* Passwords
* Session cookies
* Authentication tokens
* API keys
* Client secrets
* Database credentials

Remove excessive debugging logs before submission.

---

# 43. Security

The backend remains the security authority.

The Android application must never assume that:

```text
user_id
price
stock
cart ownership
order ownership
```

supplied by the client is trustworthy.

The Android app displays server responses.

The Flask backend validates and authorizes operations.

---

# 44. Do Not Modify Existing Backend Without Reason

The existing website already works against the backend.

Before changing backend behavior:

1. Inspect the existing implementation.
2. Determine whether Android actually requires the change.
3. Determine whether the change could break the website.
4. Test the website API behavior.
5. Make the smallest compatible change.

Do not modify backend contracts merely to make Retrofit declarations prettier.

---

# 45. Backend Compatibility

The Android app must remain compatible with the existing web frontend.

This means changes to:

```text
Authentication
Cart
Products
Orders
```

must preserve the existing API contract unless a change is explicitly approved.

If an API change is unavoidable, update both clients and test both.

---

# 46. Testing Strategy

At minimum test:

### Authentication

```text
Login success
Invalid login
Logout
Current user
Expired/invalid session
```

### Products

```text
List products
Get product
Search
Category
Sorting
Pagination
```

### Cart

```text
Get cart
Add item
Increase quantity
Decrease quantity
Remove item
Clear cart
Insufficient stock
Unauthenticated request
```

### Synchronization

```text
Web adds item
    ↓
Mobile retrieves cart
    ↓
Item appears
```

```text
Mobile adds item
    ↓
Web retrieves cart
    ↓
Item appears
```

```text
Mobile updates quantity
    ↓
Web retrieves cart
    ↓
Quantity matches
```

```text
Mobile removes item
    ↓
Web retrieves cart
    ↓
Item is absent
```

---

# 47. Physical Device Testing

HNG explicitly requires testing on a phone.

Do not consider:

```text
Android Studio emulator works
```

as completion.

Test on an actual Android device.

Verify:

```text
Internet connectivity
Login
Product loading
Images
Cart
Cart synchronization
Navigation
Error states
```

Use a real production API where appropriate for final verification.

---

# 48. Final HNG Test

The final demonstration should be capable of showing:

```text
1. Open shop website.

2. Log in.

3. Add Product A to cart.

4. Open Android app.

5. Log in using the SAME account.

6. Open mobile cart.

7. Product A appears.

8. Change quantity on mobile.

9. Return to website.

10. Website reflects the changed quantity.

11. Remove item from mobile.

12. Refresh/open website cart.

13. Item is gone.
```

This is the core proof that both applications use the same backend cart.

---

# 49. Definition of Done

Stage 3 is complete when:

* Android application is built with Kotlin.
* Jetpack Compose is used for the UI.
* The application communicates with the existing Flask API.
* Email/password login works.
* Server-side session authentication works from Android.
* Products load from the existing API.
* Product details load from the existing API.
* Authenticated cart loads from `/api/cart`.
* Cart items can be added.
* Cart quantities can be changed.
* Cart items can be removed.
* Cart can be cleared.
* Website and Android use the same authenticated cart.
* Web → mobile cart synchronization is demonstrated.
* Mobile → web cart synchronization is demonstrated.
* The application has been tested on a physical Android phone.
* No server secrets are included in the Android application.
* Existing website functionality remains intact.
* Production communication uses HTTPS.

---

# 50. Development Order

Follow this order:

```text
1. Create Android project
        ↓
2. Configure Kotlin + Compose
        ↓
3. Configure API base URL
        ↓
4. Configure Retrofit
        ↓
5. Configure OkHttp CookieJar
        ↓
6. Test GET /api/health
        ↓
7. Implement login
        ↓
8. Implement GET /api/auth/me
        ↓
9. Verify session persistence
        ↓
10. Implement product API
        ↓
11. Build product list
        ↓
12. Build product details
        ↓
13. Implement GET /api/cart
        ↓
14. Implement add-to-cart
        ↓
15. Implement quantity updates
        ↓
16. Implement removal
        ↓
17. Implement clear cart
        ↓
18. Test web → mobile synchronization
        ↓
19. Test mobile → web synchronization
        ↓
20. Implement remaining required shop flows
        ↓
21. Test on physical phone
        ↓
22. Test production API
        ↓
23. Clean logs and debug code
        ↓
24. Prepare HNG submission
```

Do not jump directly into UI polishing before proving:

```text
Android
   ↓
Retrofit
   ↓
Session Cookie
   ↓
Flask API
   ↓
PostgreSQL
```

works.

---

# 51. Scope Control

This stage is NOT an excuse to rebuild the entire shop.

Do not add:

* New backend
* New database
* Firebase authentication
* JWT authentication
* Payment gateway
* Admin dashboard
* Chat
* Push notification system
* Complex offline synchronization
* WebSocket infrastructure without a demonstrated requirement
* Unnecessary dependency frameworks
* Unnecessary architecture layers

Build the smallest complete Android client that satisfies HNG15 Lesson 3.

---

# 52. AI Agent Rules

Before modifying the project:

1. Read this `AGENT.md`.
2. Inspect the existing Android code.
3. Inspect the relevant API contract in `shop-website`.
4. Understand the existing implementation before changing it.
5. Prefer the smallest correct change.
6. Do not rewrite working code unnecessarily.
7. Do not invent API endpoints.
8. Do not change backend behavior without a demonstrated reason.
9. Do not introduce dependencies without justification.
10. Do not expose secrets.
11. Do not commit or push changes unless explicitly instructed.
12. Run relevant tests after meaningful changes.
13. Verify API behavior before blaming the Android UI.
14. Keep HNG Lesson 3 as the primary scope.

---

# 53. Critical Rule

When something does not work, debug in this order:

```text
1. Is the API reachable?
        ↓
2. Is the endpoint correct?
        ↓
3. Is the HTTP method correct?
        ↓
4. Is the request body correct?
        ↓
5. Is the session cookie being stored?
        ↓
6. Is the session cookie being sent?
        ↓
7. Is the backend accepting the session?
        ↓
8. Is the response mapped correctly?
        ↓
9. Is the ViewModel updating?
        ↓
10. Is Compose displaying the state?
```

Do not immediately rewrite the UI when the actual problem is that the phone never authenticated.

---

# 54. Project Principle

The Android application is a second client of the same shop.

The architecture must always preserve this relationship:

```text
                 ENJ0Y SHOP API
                       │
          ┌────────────┴────────────┐
          │                         │
      React Web                 Kotlin App
          │                         │
          └────────────┬────────────┘
                       │
                 Same User
                       │
                 Same Cart
                       │
                 Same Orders
```

The goal is not to make the Android application look impressive in isolation.

The goal is to prove that two different clients can reliably operate against the same backend and shared customer state.

That is the actual Lesson 3 problem.
