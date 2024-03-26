import {useLocation} from "react-router-dom";
import {useTabs} from "../../../store/useTabs.tsx";
import {useEffect} from "react";

export const useTabSpyLocation = (tabName: string) => {
  const location = useLocation()
  const pushTab = useTabs(state => state.push)
  useEffect(() => pushTab({
    href: location.pathname,
    label: tabName
  }), [location.pathname])
}
