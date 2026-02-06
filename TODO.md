# HopSpot TODO Liste für Claude Code

## Projekt-Übersicht

**App:** HopSpot - Mobile App zur Erfassung und Verwaltung von Spots  
**Tech Stack:**
- **Android:** Kotlin, Jetpack Compose, Hilt, Retrofit, Coil
- **Backend:** Go (Gin Framework), PostgreSQL, MinIO
- **Hosting:** Raspberry Pi 5, Cloudflare Tunnel

**Projekt-Dateien:**
- `/mnt/project/APP-ANFORDERUNGEN.md` - Vollständige App-Spezifikation
- `/mnt/project/API-ANFORDERUNGEN.md` - Backend API Spezifikation
- `/mnt/project/INFRASTRUKTUR.md` - Server/Deployment Dokumentation

---

## ✅ Erledigte Features

| Feature | Status |
|---------|--------|
| Map Screen mit Vollbild Google Map | ✅ |
| Custom Bier-Pin Icon (ic_beer_marker.png) | ✅ |
| GPS-zentrierte Karte | ✅ |
| Spot-Preview Card beim Marker-Klick | ✅ |
| Location Picker (Create/Edit) mit interaktiver Map | ✅ |
| Admin Panel (User & Invitation Codes) | ✅ |
| Foto Upload/Management | ✅ |
| Token Management / Auto-Login (Splash Screen) | ✅ |
| Besuchshistorie (Visits Screen + "Ich war hier") | ✅ |
| Bild-Rotation (EXIF Orientation beim Upload) | ✅ |
| Fullscreen Bildansicht (Zoom, Swipe, Dark Mode) | ✅ |
| Entfernungs-Anzeige in Liste (GPS + Sortierung) | ✅ |
| Wetter-Integration (Temperatur, Wind, Weather Icons Font) | ✅ |
| Push-Benachrichtigungen (Firebase FCM) | ✅ |
| Visits loeschen (API + App + Dialog) | ✅ |
| Offline Sync (Room, Auto-Sync, Last-Write-Wins) | ✅ |
| Zufälliger Spot Button (API + FAB in Liste) | ✅ |
| Favoriten (Herz-Icon, Favoriten-Liste, API) | ✅ |
| Activity Feed (Timeline, Pull-to-refresh) | ✅ |

---

## 📁 Wichtige Code-Dateien

**Navigation:**
- `ui/navigation/HopSpotNavGraph.kt`
- `ui/navigation/Route.kt`
- `ui/components/BottomNavigationBar.kt`

**Screens:**
- `ui/screens/map/MapScreen.kt` + `MapViewModel.kt`
- `ui/screens/spotlist/SpotListScreen.kt` + `SpotListViewModel.kt`
- `ui/screens/spotdetail/SpotDetailScreen.kt` + `SpotDetailViewModel.kt`
- `ui/screens/spotcreate/SpotCreateScreen.kt` + `SpotCreateViewModel.kt`
- `ui/screens/spotedit/SpotEditScreen.kt` + `SpotEditViewModel.kt`

**Data Layer:**
- `data/repository/SpotRepositoryImpl.kt`
- `data/remote/api/HopSpotApi.kt`
- `domain/repository/SpotRepository.kt`

**Components:**
- `ui/components/LocationPickerCard.kt` (mit interaktiver Map)
- `ui/components/PhotoPickerDialog.kt`

---

## 🔗 Backend API Base URL

```
https://hopspot.kickpaws.com/api/v1
```

**Auth Header:**
```
Authorization: Bearer <jwt-token>
```

---

## 🎨 UI/UX Verbesserungen

### Screen-Strukturen überarbeiten
- [x] Allgemeine UI-Verbesserungen (Screen-Layouts)
- [x] Konsistentere Strukturen über alle Screens

### Skeleton Loading (Liste)
- [x] Skeleton Loading in SpotList fixen/verbessern

### System Bar Farben
- [x] Bottom Navigation Bar und Status Bar Farben angleichen
- [x] Weniger starke Farbunterschiede zu System-UI

---

## 🔧 Technische Verbesserungen

### Error Handling
- [x] Besseres Error Handling (nicht nur "HTTP XYZ")
- [x] Error Codes vom Backend definieren
- [x] Benutzerfreundliche Fehlermeldungen in der App
- [x] Einheitliche Error-Response Struktur in API

### API Test Coverage
- [ ] Test Coverage erhöhen (aktuell nur 6%)
- [ ] Unit Tests für Services
- [ ] Integration Tests für Endpoints

---

## 🎉 Nice-to-Have / Bonus Features

### Mehrsprachigkeit
- [x] Alle Fehlermeldungen und Texte welche die App anzeigt sollen sich nach der Gerätesprache richten 

### Google Analytics
- [x] Einbinden da shcon Firebase vorhanden und es im gleichen Projekt geht

### Activity Feed
- [x] **API:** Feed-Endpoint (`GET /api/v1/activities`)
- [x] **API:** Activity-Tabelle (user_id, action_type, spot_id, timestamp)
- [x] **App:** Feed Screen mit Timeline (Pull-to-refresh, Infinite Scroll)
- [x] Events: "Max hat Sonnenspot hinzugefügt", "Lisa hat Parkspot besucht"

### Jahresrückblick ("Dein HopSpot 2025")
- [ ] **API:** Stats-Endpoint für Jahresübersicht
- [ ] **App:** Schöner Jahresrückblick-Screen
- [ ] Statistiken: Spots besucht, Spots erstellt, Lieblings-Spot, km gelaufen, etc.
- [ ] Shareable als Bild

### Favoriten / Merkliste
- [x] **API:** Favorites-Tabelle (user_id, spot_id)
- [x] **API:** Endpoints (`POST/DELETE /api/v1/spots/{id}/favorite`, `GET /api/v1/favorites`)
- [x] **App:** Herz-Icon auf Spot-Detail
- [x] **App:** Favoriten-Tab in Bottom Navigation

### Home Screen Widget

**Beschreibung:** Cooles Widget für den Android Homescreen.

**Ideen:**
- [ ] Nächster Spot in der Nähe anzeigen (Name, Distanz, Thumbnail)
- [ ] Schnellzugriff auf Map
- [ ] Statistiken (Anzahl Besuche, entdeckte Spots)
- [ ] Glance API für modernes Jetpack Compose Widget

---

## 🧹 Abschluss / Refactoring

### Naming Refactoring: Bench → Spot (ganz am Ende)
- [x] **API:** Datenbank-Tabellen umbenennen (benches → spots)
- [x] **API:** Endpoints umbenennen (/benches → /spots)
- [x] **API:** Go Code Naming anpassen (Bench → Spot)
- [ ] **API:** Swagger Dokumentation anpassen
- [x] **App:** Domain Models umbenennen
- [x] **App:** UI Texte anpassen ("Bank" → "Spot")
- [x] **App:** Dateinamen/Klassen umbenennen

### Codebase Cleanup (am Ende)
- [ ] Android App refactoren und gut strukturieren
- [ ] Go Backend refactoren und gut strukturieren
- [ ] Code kommentieren (beide Projekte)
- [ ] API Swagger Dokumentation aktualisieren
- [ ] Android README auf guten Stand bringen
- [ ] Backend README auf guten Stand bringen

### Warnings & Dependencies (am Ende)
- [ ] Alle Compiler-Warnings beheben (Android)
- [ ] Alle Linter-Warnings beheben (Go)
- [ ] Deprecation Warnings fixen
- [ ] Dependencies aktualisieren (Android: libs.versions.toml)
- [ ] Dependencies aktualisieren (Go: go.mod)

---

## 📝 Hinweise

- User-Präferenz: Kein scharfes ß verwenden, immer "ss"
- App-Sprache: Deutsch
- Passwörter/Secrets nie in Code committen
- API hat Limit von max. 100 Einträgen pro Request
