import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, CircularProgress, InputAdornment, Paper,
} from '@mui/material';
import { Lock, AccountBalance, Security } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';
import { toast } from 'react-toastify';

export default function ChangePinPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [oldPin, setOldPin] = useState('');
  const [newPin, setNewPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.accountNumber) return;
    setError('');

    if (newPin !== confirmPin) {
      setError('New PIN and confirm PIN do not match');
      toast.error('New PIN and confirm PIN do not match');
      return;
    }

    if (newPin.length < 4 || newPin.length > 6) {
      setError('PIN must be between 4 and 6 digits');
      toast.error('PIN must be between 4 and 6 digits');
      return;
    }

    setLoading(true);

    try {
      const response = await atmAPI.changePin(user.accountNumber, oldPin, newPin);
      toast.success('PIN changed successfully!');
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
      toast.error(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
      <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #ff9100 0%, #e65100 100%)', borderRadius: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Change PIN</Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Update your account PIN securely
        </Typography>
      </Paper>

      <Card>
        <CardContent sx={{ p: 3 }}>
          {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Current PIN"
              type="password"
              value={oldPin}
              onChange={(e) => setOldPin(e.target.value)}
              margin="normal"
              required
              inputProps={{ maxLength: 6 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <AccountBalance color="primary" />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="New PIN"
              type="password"
              value={newPin}
              onChange={(e) => setNewPin(e.target.value)}
              margin="normal"
              required
              inputProps={{ maxLength: 6 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Lock color="primary" />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="Confirm New PIN"
              type="password"
              value={confirmPin}
              onChange={(e) => setConfirmPin(e.target.value)}
              margin="normal"
              required
              inputProps={{ maxLength: 6 }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <Security color="primary" />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 3 }}
            />

            <Alert severity="warning" sx={{ mb: 3, borderRadius: 2 }}>
              <Typography variant="caption">
                PIN must be between 4 and 6 digits. Choose a PIN that is easy to remember but hard to guess.
              </Typography>
            </Alert>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={<Lock />}
              sx={{ py: 1.5 }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Change PIN'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}