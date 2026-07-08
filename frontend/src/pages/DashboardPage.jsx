import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box, Grid, Card, CardContent, Typography, Button,
  CircularProgress, Chip, IconButton, Paper,
} from '@mui/material';
import {
  AccountBalance, MoneyOff, AccountBalanceWallet, Send,
  Assessment, TrendingUp, TrendingDown, Refresh, CreditCard,
} from '@mui/icons-material';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useAuth } from '../context/AuthContext';
import { atmAPI } from '../services/api';

const actionCards = [
  { title: 'Withdraw', icon: <MoneyOff fontSize="large" />, path: '/withdraw', color: '#ff6b35' },
  { title: 'Deposit', icon: <AccountBalanceWallet fontSize="large" />, path: '/deposit', color: '#00c853' },
  { title: 'Transfer', icon: <Send fontSize="large" />, path: '/transfer', color: '#2979ff' },
  { title: 'Statement', icon: <Assessment fontSize="large" />, path: '/statement', color: '#7c4dff' },
];

export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [balance, setBalance] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [atmStatus, setAtmStatus] = useState(null);

  const loadData = async () => {
    if (!user?.accountNumber) return;
    setLoading(true);
    try {
      const [balanceRes, statementRes, statusRes] = await Promise.all([
        atmAPI.getBalance(user.accountNumber),
        atmAPI.getStatement(user.accountNumber),
        atmAPI.getATMStatus(),
      ]);
      setBalance(balanceRes.data.data);
      setTransactions(statementRes.data.data || []);
      setAtmStatus(statusRes.data.data);
    } catch (error) {
      console.error('Dashboard load error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [user?.accountNumber]);

  const chartData = transactions
    .slice(0, 10)
    .reverse()
    .map((t) => ({
      date: new Date(t.transactionDate).toLocaleDateString('en-IN', { day: '2-digit', month: 'short' }),
      amount: t.amount,
      type: t.transactionType,
    }));

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '60vh' }}>
        <CircularProgress size={60} />
      </Box>
    );
  }

  return (
    <Box>
      {/* Welcome Header */}
      <Paper
        sx={{
          p: 3, mb: 3,
          background: 'linear-gradient(135deg, #1a73e8 0%, #0d47a1 100%)',
          borderRadius: 3,
          position: 'relative',
          overflow: 'hidden',
        }}
      >
        <Box sx={{ position: 'relative', zIndex: 1 }}>
          <Typography variant="body2" sx={{ mb: 1, opacity: 0.9 }}>
            Welcome back,
          </Typography>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 2 }}>
            {balance?.accountHolderName || 'User'}
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Chip
              icon={<CreditCard />}
              label={`Account: ****${user?.accountNumber?.slice(-4) || '****'}`}
              variant="outlined"
              sx={{ color: 'white', borderColor: 'rgba(255,255,255,0.5)' }}
            />
            <Chip
              label={balance?.accountType || 'SAVINGS'}
              variant="outlined"
              sx={{ color: 'white', borderColor: 'rgba(255,255,255,0.5)' }}
            />
          </Box>
        </Box>
        <Box
          sx={{
            position: 'absolute',
            top: -20, right: -20,
            width: 200, height: 200,
            borderRadius: '50%',
            background: 'rgba(255,255,255,0.05)',
          }}
        />
      </Paper>

      {/* Balance Card */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={6}>
          <Card sx={{ p: 2 }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                Current Balance
              </Typography>
              <Typography variant="h3" sx={{ fontWeight: 700, color: 'secondary.light' }}>
                ₹{balance?.balance?.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) || '0.00'}
              </Typography>
              <Box sx={{ display: 'flex', alignItems: 'center', mt: 1 }}>
                <TrendingUp sx={{ color: 'success.main', mr: 0.5 }} fontSize="small" />
                <Typography variant="caption" color="success.main">
                  Active Account
                </Typography>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={6}>
          <Card sx={{ p: 2 }}>
            <CardContent>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                Quick Actions
              </Typography>
              <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                {actionCards.map((action) => (
                  <Button
                    key={action.title}
                    variant="outlined"
                    startIcon={action.icon}
                    onClick={() => navigate(action.path)}
                    sx={{
                      borderColor: action.color,
                      color: action.color,
                      '&:hover': {
                        borderColor: action.color,
                        bgcolor: `${action.color}15`,
                      },
                    }}
                  >
                    {action.title}
                  </Button>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Chart and Recent Transactions */}
      <Grid container spacing={3}>
        <Grid item xs={12} md={7}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>
                Transaction History
              </Typography>
              {chartData.length > 0 ? (
                <ResponsiveContainer width="100%" height={250}>
                  <LineChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.1)" />
                    <XAxis dataKey="date" stroke="#b2bac2" fontSize={12} />
                    <YAxis stroke="#b2bac2" fontSize={12} />
                    <Tooltip
                      contentStyle={{
                        background: '#132f4c',
                        border: '1px solid rgba(255,255,255,0.1)',
                        borderRadius: 8,
                      }}
                    />
                    <Line
                      type="monotone"
                      dataKey="amount"
                      stroke="#1a73e8"
                      strokeWidth={2}
                      dot={{ fill: '#1a73e8', strokeWidth: 2 }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <Box sx={{ textAlign: 'center', py: 6 }}>
                  <Typography color="text.secondary">No transactions yet</Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={5}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ mb: 2 }}>
                Recent Transactions
              </Typography>
              {transactions.slice(0, 5).map((txn, index) => (
                <Box
                  key={txn.transactionId}
                  sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    py: 1.5,
                    borderBottom: index < Math.min(transactions.length, 5) - 1 ? '1px solid rgba(255,255,255,0.1)' : 'none',
                  }}
                >
                  <Box>
                    <Typography variant="body2" sx={{ fontWeight: 500 }}>
                      {txn.transactionType}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(txn.transactionDate).toLocaleString('en-IN')}
                    </Typography>
                  </Box>
                  <Box sx={{ textAlign: 'right' }}>
                    <Typography
                      variant="body2"
                      sx={{
                        fontWeight: 600,
                        color: txn.transactionType === 'DEPOSIT' ? 'success.main' : 'error.main',
                      }}
                    >
                      {txn.transactionType === 'DEPOSIT' ? '+' : '-'}₹{txn.amount?.toLocaleString('en-IN')}
                    </Typography>
                    {txn.successful ? (
                      <Chip label="Success" size="small" color="success" variant="outlined" sx={{ height: 20, fontSize: 10 }} />
                    ) : (
                      <Chip label="Failed" size="small" color="error" variant="outlined" sx={{ height: 20, fontSize: 10 }} />
                    )}
                  </Box>
                </Box>
              ))}
              {transactions.length === 0 && (
                <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                  No transactions yet
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}