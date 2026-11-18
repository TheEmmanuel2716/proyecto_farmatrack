import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// En desarrollo, Vite reenvía las peticiones GraphQL al gateway.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const gateway = env.GATEWAY_URL || 'http://localhost:8083'

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api/graphql': {
          target: gateway,
          changeOrigin: true,
          rewrite: () => '/graphql',
        },
      },
    },
  }
})
