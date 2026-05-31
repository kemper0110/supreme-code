# Grafana dashboard sources

Grafana still provisions `../dashboards/supreme-code.json`.

Edit the smaller source files instead:

- `supreme-code/base.json` contains dashboard metadata, variables, time range, and tags.
- `supreme-code/panels/*.json` contains panel sections in dashboard order.

After editing sources, rebuild the provisioned dashboard:

```bash
node config/grafana/dashboard-src/build-supreme-code.mjs
```

