# Optional FHS environment for Java 8 and Forge 1.12.2's native libraries.
{ pkgs ? import <nixpkgs> { } }:
(pkgs.buildFHSUserEnv {
  name = "agricrops-dev";
  targetPkgs = pkgs: with pkgs; [
    jdk8 git zlib stdenv.cc.cc.lib libGL openal alsa-lib
    xorg.libX11 xorg.libXext xorg.libXcursor xorg.libXrandr
    xorg.libXxf86vm xorg.libXi xorg.libXtst
  ];
  runScript = "bash";
  profile = ''
    export JAVA_HOME="${pkgs.jdk8}"
    export PATH="$JAVA_HOME/bin:$PATH"
  '';
}).env
