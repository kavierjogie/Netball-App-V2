# NetBall App

An Android app for netball coaches to manage players, set up games, record live player actions on court, and review match statistics. Data is stored in Supabase and accessed through its REST API with Retrofit.

## Features

- **Coach accounts:** register, log in, and edit your profile
- **Player profiles:** add, update, and delete players
- **Game setup:** create a game, place players on court positions, and pick bench players
- **Live game screen:** log actions per player (goals, penalty goals, misses, goal assists, centre pass receives, intercepts, deflections, rebounds, held/dropped ball, stepping, contact, obstruction, and more) and make bench substitutions
- **Manage games:** view, update, and delete past games
- **Match analysis:** per-player stats by half, charted with MPAndroidChart

## Tech stack

- Java 8, Android SDK 34 (min SDK 24)
- Gradle 8.0, Android Gradle Plugin 8.1.3 (Kotlin DSL)
- Retrofit 2 + Gson + OkHttp
- Supabase (PostgREST): tables `coach`, `player`, `game`, `court`, `player_action`, `player_coach`, `coach_game`, and the `player_stats` view
- Material Components, MPAndroidChart

## Getting started

1. Clone the repo and open it in Android Studio.
2. Copy `.env.example` to `.env` in the project root and fill it in (Supabase dashboard → Project Settings → API):

   ```env
   SUPABASE_URL=https://<project-ref>.supabase.co
   SUPABASE_ANON_KEY=<anon public key>
   ```

   Gradle reads these into `BuildConfig.SUPABASE_URL` and `BuildConfig.SUPABASE_ANON_KEY`. If they're missing, Gradle falls back to environment variables with the same names (useful for CI), and the build fails if neither is set.
3. Run the `app` configuration on an emulator or device, or build from the command line:

   ```bash
   ./gradlew assembleDebug
   ```

> `.env` is gitignored. Never commit it, and never put a Supabase **service-role** key in this app. Anything in `BuildConfig` ships inside the APK.

## Project structure

```
app/src/main/java/com/example/netballapp/
├── activities/   Screens (login, dashboard, game setup, game screen, analysis, ...)
├── adapters/     RecyclerView adapters for players, games, bench, and stats
├── api/          RetrofitClient (base URL + auth headers) and SuperbaseAPI (endpoints)
├── listeners/    Click/action callback interfaces
└── Model/        Data classes (Coach, Player, Game, Court, PlayerAction, ...) and SessionManager
```

## Security notes

- The anon key is public by design, so access must be restricted with **Row Level Security** on every Supabase table.
- Coach login currently compares a plain-text password stored in the `coach` table. Migrating to Supabase Auth is recommended before any real-world use.
