import React, {createContext} from "react";

export type SelectedSolutionTuple = {
  solutionId: number
  languageId: string
}
export const SelectedSolutionContext = createContext<[SelectedSolutionTuple | null, React.Dispatch<React.SetStateAction<SelectedSolutionTuple | null>>]>([null, () => {
  throw new Error("SelectedSolutionContext.Provider not found")
}])
