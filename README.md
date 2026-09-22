# Handwriting Keyboard (Android IME)

A production-ready Android custom Input Method Editor (IME) based on touch/drawing handwriting input.

The user writes characters one by one on a responsive touch drawing canvas. Characters are recognized locally on-device and inserted into the active text field **WITHOUT automatically adding a space**.

---

## 🎯 Core Rule & Behavior

### **The Fundamental Rule: No Automatic Spacing**
After recognizing a character:
* `commitText("A")` is called.
* When the next character is recognized, `commitText("B")` is called.
* **Result: `AB`**
* **NEVER `A B`**

A space is **ONLY** inserted when the user explicitly taps the **`[SPACE]`** key (`InputConnection.commitText(" ", 1)`).

#### **Examples**:
* Draw `A` → Draw `B` → Draw `C` → Draw `D` ➔ **`ABCD`**
* Draw `A` → Draw `B` → Press `[SPACE]` → Draw `C` ➔ **`AB C`**
* Draw `H` `e` `l` `l` `o` → Press `[SPACE]` → Draw `W` `o` `r` `l` `d` ➔ **`Hello World`**
* Draw `H` `i` → Tap `[!]` ➔ **`Hi!`** (Punctuation also never creates spaces)

---

## 🏗️ Architecture & Project Structure

The project strictly adheres to clean Android architecture principles:

```
app/src/main/java/com/example/
├── ime/
│   ├── HandwritingInputMethodService.kt   # System InputMethodService lifecycle & Compose container
│   ├── KeyboardInputConnection.kt         # Enforces zero-space rule, backspace & cursor editing
│   └── ImeConfig.kt                       # Palettes, actions, and IME constants
│
├── handwriting/
│   ├── Point.kt                           # Coordinate, timestamp, and pressure point
│   ├── Stroke.kt                          # Single continuous stroke with bounding box & length
│   ├── StrokeManager.kt                   # Multi-stroke collection with undo, redo, and clear
│   ├── StrokeNormalizer.kt                # Resampling & normalization to [0..1] x [0..1]
│   ├── CharacterRecognizer.kt             # Modular recognition engine interface
│   └── HandwritingCanvasView.kt           # Hardware-accelerated drawing view with Bézier curves
│
├── recognition/
│   ├── Candidate.kt                       # Ranked candidate with space-rejection invariant
│   ├── RecognitionResult.kt               # Result metadata with latency & confidence
│   ├── RecognitionConfig.kt               # Languages (EN, AR) & Modes (Auto, 123, Letters, Symbols)
│   ├── GestureTemplateLibrary.kt          # Normalized reference gestures (A-Z, a-z, 0-9, Arabic, symbols)
│   └── LocalCharacterRecognizer.kt        # Deterministic 100% on-device geometric & template recognizer
│
├── ui/
│   ├── KeyboardView.kt                    # Material 3 Keyboard layout (Canvas, toolbar, space, backspace)
│   └── CandidateBar.kt                    # Horizontal scrollable candidate chips & punctuation bar
│
├── settings/
│   └── SettingsRepository.kt              # SharedPreferences persistence & StateFlow observables
│
└── MainActivity.kt                        # Companion app: IME setup guide, live sandbox & settings
```

---

## 🚀 Installation & Setup Instructions

### 1. Enable Keyboard in Android Settings
1. Open Android **Settings**.
2. Navigate to **System** > **Languages & input** (or **General Management** > **Keyboard list and default** on Samsung devices).
3. Tap **On-screen keyboard** > **Manage on-screen keyboards**.
4. Toggle **Handwriting Keyboard** to **ON**.
5. Confirm the standard Android system dialog for custom input methods.

*(Alternatively, open the companion app and tap **"1. Enable in System Settings"**)*.

### 2. Switch to Handwriting Keyboard
1. Open any application with a text field (Notes, Messages, Chrome, WhatsApp, Forms).
2. Tap inside any text field to bring up the keyboard.
3. Tap the **Keyboard icon** located in the system navigation bar (bottom right on Android 10+), or swipe down the notification shade and tap **"Choose input method"**.
4. Select **Handwriting Keyboard**.

*(Alternatively, open the companion app and tap **"2. Select as Active Keyboard"**)*.

---

## ✍️ How to Use the Keyboard

1. **Drawing Characters**:
   * Draw directly on the touch canvas using your finger or stylus.
   * Smooth Bézier curve interpolation renders ink with zero latency.
2. **Multi-Stroke Characters**:
   * For characters requiring multiple strokes (`A`, `E`, `H`, `t`, `k`, `X`, `i`, `?`), draw the first stroke, lift your finger, and immediately draw subsequent strokes.
   * The recognizer waits for the **Recognition Delay** (default: 600ms) before committing.
   * To commit immediately without waiting, tap the **`[✓]`** checkmark button in the canvas overlay.
3. **Candidate Selection**:
   * The candidate bar displays top matched characters with confidence percentages.
   * Tapping any candidate immediately commits that character with **NO SPACE**.
4. **Inserting Spaces**:
   * Press the wide **`[SPACE]`** key. Each press inserts exactly one space.
5. **Deleting**:
   * Tap **`[⌫]`** to delete the character before the cursor.
   * Handles Unicode surrogate pairs (e.g. emojis) properly.
6. **Modes & Languages**:
   * Tap **`AUTO / 123 / ABC / #$=`** to toggle recognition mode.
   * Tap **`EN / عربي`** to switch between English and Arabic handwriting.
   * Tap **`◀ / ▶`** to navigate cursor position within text.

---

## ⚙️ Configurable Settings

Access settings either by tapping the **Gear icon** on the keyboard, or by opening the companion app:

| Setting | Default | Description |
| :--- | :--- | :--- |
| **Recognition Delay** | `600 ms` | Inactivity window before multi-stroke recognition finishes |
| **Stroke Width** | `7 dp` | Visual thickness of drawing strokes |
| **Auto-Confirm Character** | `ON` | Automatically commits top recognized character when delay finishes |
| **Automatic Space After Character** | `OFF` | Strictly `OFF`. Core rule enforces no trailing spaces |
| **Baseline Guides** | `ON` | Displays ascender, midline, and baseline guidelines on canvas |
| **Vibration / Haptics** | `ON` | Subtle tactile response on touches and key presses |
| **Keyboard Height** | `320 dp` | Height of the keyboard surface |
| **Themes** | `Midnight Cyan` | Midnight Cyan, Dark Purple, Light Blue |
| **Language** | `English` | English (Latin script), Arabic (عربي) |
| **Developer / Debug Mode** | `OFF` | Real-time display of stroke count, latency, and raw coordinates |

---

## 🔒 Security & Privacy Guarantee

* **100% Offline**: The app declares **NO `android.permission.INTERNET`**. It cannot send network requests.
* **No Telemetry / No Logging**: Strokes, keystrokes, and text inputs are processed entirely in memory on the CPU and never logged or written to storage.
* **Password Isolation**: In password input fields, suggestions and history are completely disabled.

---

## 🛠️ Troubleshooting

| Issue | Solution |
| :--- | :--- |
| **Keyboard does not appear in keyboard list** | Go to **Settings > System > Languages & input > On-screen keyboard > Manage keyboards** and ensure **Handwriting Keyboard** is toggled **ON**. |
| **Multi-stroke letters split into two characters** | Increase **Recognition Delay** in the Settings tab to `900ms` or `1200ms` to give yourself more time between strokes. |
| **Need to switch back to standard keyboard** | Tap the small keyboard icon at the bottom-right corner of the Android navigation bar while typing. |

---

## 🧪 Testing

Automated local tests verify Critical User Journeys (CUJs) on the JVM via Robolectric:
```bash
gradle :app:testDebugUnitTest
```
Tests cover:
* `ABCD` sequential commit without space.
* `AB C` with explicit space.
* `Hello World` sequence.
* Prevention of `commitText(char + " ")`.
* Backspace & Unicode surrogate pairs deletion.
* Punctuation without trailing space.
* Multi-stroke manager operations (undo, redo, clear).
