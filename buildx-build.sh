#!/usr/bin/env bash
set -euo pipefail

args=(
  --file src/main/docker/Dockerfile.dev
  --tag "$IMAGE"
  --output "type=image,push=true,registry.insecure=true"
)

docker buildx build "${args[@]}" "$BUILD_CONTEXT"
