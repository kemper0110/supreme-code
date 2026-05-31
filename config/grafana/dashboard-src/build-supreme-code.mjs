import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const sourceDir = path.dirname(fileURLToPath(import.meta.url));
const dashboardDir = path.join(sourceDir, "supreme-code");
const panelsDir = path.join(dashboardDir, "panels");
const targetPath = path.resolve(sourceDir, "..", "dashboards", "supreme-code.json");

const readJson = (filePath) => JSON.parse(fs.readFileSync(filePath, "utf8"));

const base = readJson(path.join(dashboardDir, "base.json"));
const panelFiles = fs
  .readdirSync(panelsDir)
  .filter((fileName) => fileName.endsWith(".json"))
  .sort();

const panels = panelFiles.flatMap((fileName) => readJson(path.join(panelsDir, fileName)));

const dashboard = {
  ...base,
  panels,
};

fs.writeFileSync(targetPath, `${JSON.stringify(dashboard, null, 2)}\n`);
console.log(`Built ${path.relative(process.cwd(), targetPath)} from ${panelFiles.length} panel fragments.`);
