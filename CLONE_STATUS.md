# Kara-okay clone — reconstruction status

Reference: uploaded `base.apk` (Kara-okay 1.9.0).

This is the first editable reconstruction scaffold. It preserves the reference app's identified package namespace, version branding, bundled assets, audio files, native libraries, remote HTML, and a Compose-based UI foundation.

## Identified reference features
- Kara-okay 1.9.0
- Native Jetpack Compose UI
- CameraX integration
- Karaoke/native audio engine (`libkaraoke.so`, FluidSynth)
- Local song library and folders
- Remote/controller functionality
- Scoring audio and microphone scoring
- Keypad audio
- GCash support panel/QR
- Background media
- Bundled fonts

## Next reconstruction pass
1. Decode the original manifest/resources.
2. Map the original Compose screens and navigation from `okay.kara.*` classes.
3. Recreate the exact screen layouts and state flows.
4. Reconnect karaoke playback/scoring/native engine behavior.
5. Compare each screen against the reference APK before any custom changes.
