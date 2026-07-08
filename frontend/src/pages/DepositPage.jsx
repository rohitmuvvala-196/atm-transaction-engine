import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Card, CardContent, TextField, Button, Typography,
  Alert, CircularProgress, InputAdornment, Paper,
} from '@mui/material';
import { AccountBalanceWallet, Info } from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';
import { toast } from 'react-toastify';

export default function DepositPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [amount, setAmount] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.accountNumber) return;
    setError('');
    setLoading(true);

    try {
      const response = await atmAPI.deposit(user.accountNumber, parseFloat(amount));
      toast.success(`₹${amount} deposited successfully!`);
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
      <Paper sx={{ p: 3, mb: 3, background: 'linear-gradient(135deg, #00c853 0%, #009624 100%)', borderRadius: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>Deposit Funds</Typography>
        <Typography variant="body2" sx={{ opacity: 0.9 }}>
          Deposit cash into your account
        </Typography>
      </Paper>

      <Card>
        <CardContent sx={{ p: 3 }}>
          {error && <Alert severity="error" sx={{ mb: 2, borderRadius: 2 }}>{error}</Alert>}

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="Deposit Amount"
              type="number"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              margin="normal"
              required
              InputProps={{
                startAdornment: <InputAdornment position="start">₹</InputAdornment>,
                inputProps: { min: 100, step: 1 },
              }}
              sx={{ mb: 3 }}
            />

            <Alert severity="info" sx={{ mb: 3, borderRadius: 2 }} icon={<Info />}>
              <Typography variant="caption">
                Minimum deposit: ₹100 | Maximum deposit: ₹5,00,000 per transaction
              </Typography>
            </Alert>

            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              startIcon={<AccountBalanceWallet />}
              sx={{
                py: 1.5,
                bgcolor: '#00c853',
                '&:hover': { bgcolor: '#009624' },
              }}
            >
              {loading ? <CircularProgress size={24} color="inherit" /> : 'Deposit Cash'}
            </Button>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}