import React from "react";
import {useAuthContext} from "./AuthContext.jsx";
import {Navigate} from "react-router-dom";
import {Box, CircularProgress} from "@mui/material";

const AvailableAfterLoginRoute = ({children}) => {
    const {auth, isLoading} = useAuthContext();

    if (isLoading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    return auth.isAuthenticated
        ? children
        : <Navigate to="/login"/>;
};

export default AvailableAfterLoginRoute;