# 2026-10-01 vertical-slice verification

- `./gradlew test`: successful, 14 tests, zero failures/errors.
- `./gradlew runServer`: Forge loaded six mods, registered eleven plants (including Weed and
  Winter Wheat), registered `wheat_beetroot_to_winter_wheat`, and reached `Done (1.124s)`.
- `./gradlew runClient`: earlier in this working tree, the atlas reported no
  `FML.TEXTURE_ERRORS`; a wheat seed planted in crop sticks rendered as a sprout.
- `/blockdata` reported the planted crop as `agri_seed:"wheat"` with stats
  `[1,1,1,0,1,0,0,0]`, proving the species template reached the tile NBT.
- Minecraft then saved chunks and shut down cleanly. A visual post-reload check and a naturally
  rolled Winter Wheat cross remain open acceptance items.
- A later automated client close produced LWJGL `BadWindow` after the X11 window was destroyed.
  This was caused by the QA window-control method, after Forge and the atlas had loaded, and is not
  counted as a mod crash or as a successful graceful client shutdown.
