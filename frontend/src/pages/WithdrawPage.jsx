import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, CircularProgress, InputAdornment, Grid, Paper,
} from '@mui/material';
import { MoneyOff, AccountBalance, Info } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';
import { toast } from 'react-toastify';

const quickAmounts = [500, 1000, 2000, 5000, 10000, 20000];

export default function WithdrawPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [pin, setPin] = useState('');
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.accountNumber) return;
    setError('');
    setLoading(true);

    try {
      const response = await atmAPI.withdraw(user.accountNumber, pin, parseFloat(amount));
      toast.success(`₹${amount} withdrawn successfully!`);
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
      <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #ff6b35 0%, #e65100 100%)', borderRadius: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Withdraw Cash</Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Please enter your PIN and the amount you want to withdraw
        </Typography>
      </Paper>

      <Card>
        <CardContent sx={{ p: 3 }}>
          {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

          <Box sx={{ mb: 3 }}>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              Quick Amount Selection
            </Typography>
            <Grid container spacing={1}>
              {quickAmounts.map((amt) => (
                <Grid item key={amt}>
                  <Button
                    variant={amount === amt.toString() ? 'contained' : 'outlined'}
                    onClick={() => setAmount(amt.toString())}
                    size="small"
                  >
                    ₹{amt.toLocaleString('en-IN')}
                  </Button>
                </Grid>
              ))}
            </Grid>
          </Box>

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Amount"
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              margin="normal"
              required
              InputProps={{
                startAdornment: <InputAdornment position="start">₹</InputAdornment>,
                inputProps: { min: 100, step: 100 },
              }}
              sx={{ mb: 2 }}
            />

            <TextField
              fullWidth
              label="PIN"
              type="password"
              value={pin}
              onChange={(e) => setPin(e.target.value)}
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
              sx={{ mb: 3 }}
            />

            <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }} icon={<Info />}>
              <Typography variant="caption">
                Amounts must be in multiples of ₹100. Daily withdrawal limit: ₹1,00,000
              </Typography>
            </Alert>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={<MoneyOff />}
              sx={{
                py: 1.5,
                bgcolor: '#ff6b35',
                '&:hover': { bgcolor: '#e65100' },
              }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Withdraw Cash'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}