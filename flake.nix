{
  description = "ourbreak — Java 21 + Gradle dev environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; };
        jdk = pkgs.jdk21;
      in
      {
        devShells.default = pkgs.mkShell {
          name = "ourbreak";

          packages = with pkgs; [
            jdk
            gradle

            # Useful extras
            git
          ];

          # Point Gradle and tools at the pinned JDK
          JAVA_HOME = "${jdk}";

          shellHook = ''
            echo "☕  Java $(java -version 2>&1 | head -1)"
            echo "🐘  Gradle $(gradle --version 2>/dev/null | grep '^Gradle' | head -1)"
          '';
        };
      });
}
