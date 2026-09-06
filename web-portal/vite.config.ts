import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import { pidManagerPlugin } from './PidTracker.ts';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), pidManagerPlugin()],
  server: {
    port: 5555,
    // requests on /api will automatically go to 8080
    proxy: { '/api': 'http://localhost:8080' },
  },
})
