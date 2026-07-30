import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Router, NavigationEnd } from '@angular/router';
import { SidebarService } from '../../services/sidebar.service';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  currentUser: any = null;
  isOpen = false;

  constructor(
    private authService: AuthService, 
    private router: Router,
    private sidebarService: SidebarService
  ) {}

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    this.sidebarService.isOpen$.subscribe(open => {
      this.isOpen = open;
    });

    // Automatically close sidebar when navigating to a new route on mobile
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.sidebarService.close();
    });
  }

  closeSidebar() {
    this.sidebarService.close();
  }

  toggleSidebar() {
    this.sidebarService.toggle();
  }

  logout() {
    this.sidebarService.close();
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
