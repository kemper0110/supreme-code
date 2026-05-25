import {fetchPlatformConfig} from "../shared/PlatformConfig.ts";

export async function PlaygroundLoader() {
  return await fetchPlatformConfig()
}
