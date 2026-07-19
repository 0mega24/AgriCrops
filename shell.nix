# shell.nix — FHS dev environment for building Agricrops on NixOS.
#
# Build stack: RetroFuturaGradle (RFG) + Gradle 8.9, targeting Forge 1.12.2 / Java 8.
#
# Why an FHS sandbox: RFG's build itself is mostly pure-JVM, but `runClient` /
# `runServer` launch real Minecraft, which dlopens prebuilt LWJGL/GLFW/OpenAL
# natives that assume a standard FHS layout NixOS doesn't provide. This sandbox
# makes those behave as on a "normal" distro. (Alternative: enable programs.nix-ld
# system-wide — see crop-mod-getting-started.md §1.)
#
# Usage:
#   nix-shell                  # enter the FHS env (bash), JAVA_HOME -> jdk8
#   ./gradlew build            # decompiles MC (first run) and builds the mod jar
#   ./gradlew runClient        # launch a dev client (uses the natives this shell provides)
#
# Forge 1.12.2 targets Java 8; Gradle 8.9 runs fine on it. If `pkgs.jdk8` is not on
# your channel, swap in `pkgs.temurin-bin-8` or `pkgs.zulu8`.

{ pkgs ? import <nixpkgs> { } }:

(pkgs.buildFHSUserEnv {
  name = "agricrops-dev";

  targetPkgs = pkgs: (with pkgs; [
    jdk8            # Forge 1.12.2 toolchain — Java 8
    git

    # Native libraries LWJGL2 / GLFW expect to dlopen at runClient time:
    zlib
    stdenv.cc.cc.lib
    libGL
    glfw
    openal
    alsa-lib
    xorg.libX11
    xorg.libXext
    xorg.libXcursor
    xorg.libXrandr
    xorg.libXxf86vm
    xorg.libXi
    xorg.libXtst
  ]);

  runScript = "bash";

  profile = ''
    export JAVA_HOME="${pkgs.jdk8}"
    export PATH="$JAVA_HOME/bin:$PATH"
    echo "agricrops FHS dev env ready — $(java -version 2>&1 | head -n1)"
  '';
}).env
