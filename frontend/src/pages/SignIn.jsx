import {Box, Button, Card, Link, Divider} from "@mui/material";
import React, {useState} from "react";
import Typography from "@mui/material/Typography";
import ValidatedUsernameTextField from "../components/InputElements/TextField/ValidatedUsernameTextField.jsx";
import ValidatedPasswordField from "../components/InputElements/TextField/ValidatedPasswordField.jsx";
import {useNavigate} from "react-router-dom";
import {sendLoginForm} from "../services/fetch/unauth/SendLoginForm.js";
import {useAuthContext} from "../context/Auth/AuthContext.jsx";
import {useNotification} from "../context/Notification/NotificationProvider.jsx";
import UnauthorizedException from "../exception/UnauthorizedException.jsx";
import NotFoundException from "../exception/NotFoundException.jsx";
import BadRequestException from "../exception/BadRequestException.jsx";

export const SignIn = () => {

    const shouldValidate = window.APP_CONFIG.validateLoginForm;

    const {login} = useAuthContext();

    const [username, setUsername] = useState('');
    const [usernameError, setUsernameError] = useState('');

    const [password, setPassword] = useState('');
    const [passwordError, setPasswordError] = useState('');

    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();
    const {showError, showInfo, showWarn} = useNotification();

    const handleSubmit = async (e) => {
        if (e) e.preventDefault();

        const requestData = {username, password};

        try {
            setLoading(true);
            const profile = await sendLoginForm(requestData);
            login(profile);
            showInfo("Вход успешно выполнен", 4000);
        } catch (error) {
            switch (true) {
                case error instanceof UnauthorizedException:
                case error instanceof NotFoundException:
                case error instanceof BadRequestException:
                    showWarn(error.message);
                    setUsernameError(error.message);
                    break;
                default:
                    showError("Ошибка при попытке входа. Попробуйте позже");
                    console.log('Unknown error occurred! ', error);
            }
        } finally {
            setLoading(false);
        }
    };

    const shouldShowPasswordField = !usernameError && username.length > 0;
    const shouldShowButton = !passwordError && shouldShowPasswordField && password.length > 0;

    return (
        <Card variant="outlined"
              sx={{
                  padding: 3,
                  boxShadow: 3,
                  position: 'fixed',
                  top: '20%',
                  backgroundColor: 'searchInput',
                  alignSelf: 'center',
                  borderRadius: 2,
                  width: {xs: '85%', sm: '400px'},
                  height: 'auto'
              }}>

            <Typography component="h1" variant="h4" sx={{textAlign: 'center', mb: 2}}>
                Вход
            </Typography>

            <form onSubmit={handleSubmit}>
                <Box sx={{display: 'flex', flexDirection: 'column', gap: 2}}>

                    <ValidatedUsernameTextField
                        username={username}
                        setUsername={setUsername}
                        usernameError={usernameError}
                        setUsernameError={setUsernameError}
                        shouldValidate={shouldValidate}
                    />

                    <ValidatedPasswordField
                        password={password}
                        setPassword={setPassword}
                        passwordError={passwordError}
                        setPasswordError={setPasswordError}
                        shouldValidate={shouldValidate}
                    />

                    <Button
                        loadingPosition="center"
                        fullWidth
                        type="submit"
                        loading={loading}
                        disabled={shouldValidate && !shouldShowButton}
                        variant="contained"
                    >
                        Войти
                    </Button>

                    {/* РАЗДЕЛИТЕЛЬ И КНОПКА GOOGLE */}
                    <Divider sx={{ my: 1 }}>или</Divider>

                    <Button
                        fullWidth
                        variant="outlined"
                        onClick={() => {
                            window.location.href = `${window.APP_CONFIG.baseUrl}${window.APP_CONFIG.baseApi}/../oauth2/authorization/google`;
                        }}
                        sx={{
                            color: 'text.primary',
                            borderColor: 'divider',
                            textTransform: 'none',
                            fontSize: '16px',
                            fontWeight: 500,
                            py: 1,
                            '&:hover': {
                                backgroundColor: 'rgba(0,0,0,0.04)',
                                borderColor: 'text.secondary'
                            }
                        }}
                        startIcon={
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M22.56 12.25C22.56 11.47 22.49 10.72 22.36 10H12V14.26H17.92C17.66 15.63 16.88 16.78 15.69 17.57V20.34H19.26C21.35 18.42 22.56 15.6 22.56 12.25Z" fill="#4285F4"/>
                                <path d="M12 23C14.97 23 17.46 22.02 19.26 20.34L15.69 17.57C14.71 18.23 13.46 18.63 12 18.63C9.17 18.63 6.78 16.72 5.92 14.17H2.23V17.03C4.03 20.6 7.72 23 12 23Z" fill="#34A853"/>
                                <path d="M5.92 14.17C5.7 13.51 5.57 12.77 5.57 12C5.57 11.23 5.7 10.49 5.92 9.83V6.97H2.23C1.49 8.44 1.05 10.15 1.05 12C1.05 13.85 1.49 15.56 2.23 17.03L5.92 14.17Z" fill="#FBBC05"/>
                                <path d="M12 5.38C13.62 5.38 15.06 5.93 16.21 7.02L19.34 3.89C17.45 2.14 14.97 1.05 12 1.05C7.72 1.05 4.03 3.4 2.23 6.97L5.92 9.83C6.78 7.28 9.17 5.38 12 5.38Z" fill="#EA4335"/>
                            </svg>
                        }
                    >
                        Войти через Google
                    </Button>

                    <Typography variant="body1" component="p"
                                sx={{
                                    width: '100%',
                                    textAlign: 'center',
                                    mt: 1
                                }}>
                        Еще нет аккаунта?{' '}
                        <Link sx={{color: '#1976d2', cursor: 'pointer'}}
                              onClick={() => navigate("/registration")}>
                            Регистрация
                        </Link>
                    </Typography>

                </Box>
            </form>
        </Card>
    )
}