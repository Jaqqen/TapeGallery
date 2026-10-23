# Script: dev-netlify

## How to use
1. Execute `npm run dev-netlify`
   1. Execution location is irrelevant, it will automatically create the files at the right location
   2. `npm run dev-netlify:fresh` - if the `netlify.toml` for development purposes should be created anew
2. The script will create a `netlify.toml` inside this dev directory
3. Afterward, spins up a local netlify environment 

## Purpose
To simulate netlify for development purposes without touching the **prod** `netlify.toml`.
> Example: Instead of redirecting requests to a prod backend-server the request go to a dev-only server.

## Reason
Netlify does not support a native way to set up different service-environments through a single `netlify.toml`.
> Example: Deploying the app on a dev and eventually prod service-environment have to be handled 
> by 2 separate `netlify.toml`s.
