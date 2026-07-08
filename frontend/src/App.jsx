import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import WithdrawPage from './pages/WithdrawPage';
import DepositPage from './pages/DepositPage';
import TransferPage from './pages/TransferPage';
import StatementPage from './pages/StatementPage';
import ChangePinPage from './pages/ChangePinPage';
import ErrorPage from './pages/ErrorPage';
import Layout from './components/Layout';

function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/" element={
        <ProtectedRoute>
          <Layout />
        </ProtectedRoute>
      }>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="withdraw" element={<WithdrawPage />} />
        <Route path="deposit" element={<DepositPage />} />
        <Route path="transfer" element={<TransferPage />} />
        <Route path="statement" element={<StatementPage />} />
        <Route path="change-pin" element={<ChangePinPage />} />
      </Route>
      <Route path="*" element={<ErrorPage />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  );
}