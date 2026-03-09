import React from "react";
import {useAuthContext} from "./AuthContext.jsx";
import {Navigate} from "react-router-dom";
import {Box, CircularProgress} from "@mui/material";

const UnavailableAfterLoginRoute = ({children}) => {
    const {auth, isLoading} = useAuthContext();

    if (isLoading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
                <CircularProgress />
            </Box>
        );
    }

    return auth.isAuthenticated
        ? <Navigate to="/files"/>
        : children;
};

export default UnavailableAfterLoginRoute;