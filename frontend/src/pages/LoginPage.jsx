import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  TextField,
  Button,
  Typography,
  Avatar,
  Alert,
  InputAdornment,
  IconButton,
  CircularProgress,
} from '@mui/material';
import {
  AccountBalance,
  Visibility,
  VisibilityOff,
  CreditCard,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';
import { toast } from 'react-toastify';

export default function LoginPage() {
  const [accountNumber, setAccountNumber] = useState('');
  const [pin, setPin] = useState('');
  const [showPin, setShowPin] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError('');
    setLoading(true);

    try {
      const response = await atmAPI.login(accountNumber, pin);
      const data = response.data.data;

      login({
        accountNumber: data.accountNumber,
        accountHolderName: data.accountHolderName,
        token: data.token,
      });

      toast.success(`Welcome, ${data.accountHolderName}!`);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        p: 2,
        background:
          'linear-gradient(135deg, #0a1929 0%, #1a3a5c 50%, #0d47a1 100%)',
      }}
    >
      <Card
        sx={{
          maxWidth: 450,
          width: '100%',
          p: 3,
          bgcolor: 'rgba(19,47,76,0.9)',
          border: '1px solid rgba(255,255,255,0.1)',
          backdropFilter: 'blur(10px)',
        }}
      >
        <CardContent>
          <Box sx={{ textAlign: 'center', mb: 4 }}>
            <Avatar
              sx={{
                width: 80,
                height: 80,
                mx: 'auto',
                mb: 2,
                bgcolor: 'primary.main',
                boxShadow: '0 8px 32px rgba(26,115,232,.3)',
              }}
            >
              <AccountBalance sx={{ fontSize: 40 }} />
            </Avatar>

            <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
              ATM Engine
            </Typography>

            <Typography variant="body2" color="text.secondary">
              Insert your card and enter your credentials
            </Typography>
          </Box>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              required
              label="Account Number"
              margin="normal"
              value={accountNumber}
              onChange={(e) => setAccountNumber(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <CreditCard color="primary" />
                  </InputAdornment>
                ),
              }}
            />

            <TextField
              fullWidth
              required
              margin="normal"
              label="PIN"
              type={showPin ? 'text' : 'password'}
              value={pin}
              onChange={(e) => setPin(e.target.value)}
              inputProps={{
                maxLength: 6,
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <AccountBalance color="primary" />
                  </InputAdornment>
                ),
                endAdornment: (
                  <InputAdornment position="end">
                    <IconButton
                      onClick={() => setShowPin(!showPin)}
                      edge="end"
                    >
                      {showPin ? <VisibilityOff /> : <Visibility />}
                    </IconButton>
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />

            <Button
              fullWidth
              type="submit"
              variant="contained"
              size="large"
              disabled={loading}
              sx={{
                py: 1.5,
                fontSize: '1.1rem',
                background:
                  'linear-gradient(45deg,#1a73e8 30%,#4a9eff 90%)',
              }}
            >
              {loading ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                'Login'
              )}
            </Button>
          </form>

          <Box
            sx={{
              mt: 3,
              p: 2,
              borderRadius: 2,
              bgcolor: 'rgba(255,255,255,0.05)',
            }}
          >
            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ display: 'block', mb: 1 }}
            >
              Test Credentials:
            </Typography>

            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ display: 'block' }}
            >
              Account: ACC1234567890 | PIN: 1234
            </Typography>

            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ display: 'block' }}
            >
              Account: ACC0987654321 | PIN: 5678
            </Typography>

            <Typography
              variant="caption"
              color="text.secondary"
              sx={{ display: 'block' }}
            >
              Account: ACC1122334455 | PIN: 4321
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}