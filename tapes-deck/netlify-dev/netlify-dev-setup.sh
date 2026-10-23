#!/usr/bin/env bash

SCRIPT_DIR="$(cd "$(dirname "${0}")" && pwd)"
NETLIFY_TOML="netlify.toml"
DEV_NETLIFY_TOML="${SCRIPT_DIR}/${NETLIFY_TOML}"
DEV_NETLIFY_CHECK_FILE="${SCRIPT_DIR}/netlify-dev-check-file"

echo "${DEV_NETLIFY_TOML}"
echo "${DEV_NETLIFY_CHECK_FILE}"

# Deletes files for the dev environment to trigger a fresh setup of this dir
while [[ $# -gt 0 ]]; do
  case "$1" in
    -f)
      rm -f "${DEV_NETLIFY_TOML}"
      rm -f "${DEV_NETLIFY_CHECK_FILE}"
      shift # Move to the next argument
      ;;
    *)
      echo "Unknown argument: $1 --- continuing to next arg or setup execution"
      shift
      ;;
  esac
done

# Indicator for 'the dev environment profile is already set'
if [[ -f "${DEV_NETLIFY_CHECK_FILE}" ]]; then
  exit 0;
fi

# Sets the redirect target for dev environment profile
# TODO: add skill that automatically identifies the target and sets it here when this dev
# TODO:   directory is set up. Right now, the target has to be hard-coded here
sed 's|<DEV_redirects_placeholder>|tapes-hub-dev.onrender.com|g' \
  "${DEV_NETLIFY_TOML}.template" >> "${DEV_NETLIFY_TOML}"

# TODO: add port for vite proxy dynamically - toml-default: 5137, doesn't match project 5555
# Needed to tell 'netlify dev' that the local vite server is not running on the default 5137
toml set --toml-path "${DEV_NETLIFY_TOML}" --to-int dev.targetPort 5555


# a check file to see if a setup via this script is necessary or not
date '+%Y-%m%b-%d %H:%M:%S %Z%z' >> "${DEV_NETLIFY_CHECK_FILE}";
echo "netlify-dir is set up" >> "${DEV_NETLIFY_CHECK_FILE}"
