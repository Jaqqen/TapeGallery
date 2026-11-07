# /// script
# requires-python = ">=3.12"
# dependencies = [
#   "python-dotenv"
# ]
# ///
"""
This script sets the redirects[0].to value inside the netlify.toml file.
This ensures that development can be run independently of CI-releases.
"""
from pathlib import Path

import tomlkit
from dotenv import dotenv_values

path_of_this_py = Path(__file__)
dev_env_vals = dotenv_values(path_of_this_py.with_name(".env"))

netlify_toml = path_of_this_py.with_name("netlify.toml")

with netlify_toml.open('r') as f:
    data = tomlkit.parse(f.read())

data['redirects'][0]['to'] = dev_env_vals['REDIRECTS_TO']

with netlify_toml.open('w') as f:
    f.write(tomlkit.dumps(data))
