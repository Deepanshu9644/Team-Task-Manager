import { Outlet, Link, useLocation } from "react-router";
import { useAuth } from "../contexts/AuthContext";
import { LayoutDashboard, FolderKanban, LogOut, User } from "lucide-react";

export default function Layout() {
  const { user, logout } = useAuth();
  const location = useLocation();

  const isActive = (path: string) => {
    return location.pathname === path;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-8">
              <h1 className="text-xl text-indigo-600">TaskFlow</h1>
              <div className="flex items-center gap-4">
                <Link
                  to="/dashboard"
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                    isActive("/dashboard")
                      ? "bg-indigo-50 text-indigo-600"
                      : "text-gray-600 hover:bg-gray-50"
                  }`}
                >
                  <LayoutDashboard className="w-5 h-5" />
                  Dashboard
                </Link>
                <Link
                  to="/projects"
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg transition-colors ${
                    isActive("/projects") || location.pathname.startsWith("/projects/")
                      ? "bg-indigo-50 text-indigo-600"
                      : "text-gray-600 hover:bg-gray-50"
                  }`}
                >
                  <FolderKanban className="w-5 h-5" />
                  Projects
                </Link>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="flex items-center gap-2 px-3 py-2 bg-gray-50 rounded-lg">
                <User className="w-4 h-4 text-gray-600" />
                <span className="text-sm text-gray-900">{user?.name}</span>
                <span className="text-xs px-2 py-0.5 bg-indigo-100 text-indigo-700 rounded">
                  {user?.role}
                </span>
              </div>
              <button
                onClick={logout}
                className="flex items-center gap-2 px-3 py-2 text-gray-600 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              >
                <LogOut className="w-5 h-5" />
                Logout
              </button>
            </div>
          </div>
        </div>
      </nav>

      <Outlet />
    </div>
  );
}
