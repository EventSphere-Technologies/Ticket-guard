import { Injectable } from '@angular/core';
import { CanActivate, Router, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean | UrlTree | Observable<boolean | UrlTree> | Promise<boolean | UrlTree> {
    if (this.authService.isLoggedIn()) {
      // Check if admin is required
      if (route.data && route.data['role'] === 'ADMIN' && !this.authService.isAdmin()) {
        this.router.navigate(['/dashboard']);
        return false;
      }
      return true;
    }

    this.router.navigate(['/login']);
    return false;
  }
}
