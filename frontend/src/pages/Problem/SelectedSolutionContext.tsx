import React, {createContext} from "react";
import {Solution} from "./Loader.tsx";

export const SelectedSolutionContext = createContext<[Solution | null, React.Dispatch<React.SetStateAction<Solution | null>>]>([null, () => {
  throw new Error("SelectedSolutionContext.Provider not found")
}])
