import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react-swc';
import { pidManagerPlugin } from './PidTracker.ts';

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), pidManagerPlugin()],
})
