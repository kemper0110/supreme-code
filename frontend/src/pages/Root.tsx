import {LoadingOverlay} from "@mantine/core";
import {useNavigate} from "react-router-dom";
import {useEffect} from "react";

function Root() {
  const navigate = useNavigate()
  useEffect(() => navigate('/playground'), [])
  return <LoadingOverlay visible={true} zIndex={1000} overlayProps={{ radius: "sm", blur: 2 }} />
}

export default Root
