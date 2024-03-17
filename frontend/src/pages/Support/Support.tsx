import {useNavigate} from "react-router-dom";
import {useEffect} from "react";

export function Support() {
  const navigate = useNavigate()
  useEffect(() => {
    navigate("/404")
  }, [])

  return null
}
