import { Component } from '@angular/core';

@Component({
  selector: 'app-admin-settings',
  templateUrl: './admin-settings.component.html',
  styleUrls: ['./admin-settings.component.css']
})
export class AdminSettingsComponent {
  // System configurations
  botScanThreshold = 85.0;
  lockExpiryMinutes = 5;
  rapidBookingWindowMinutes = 5;
  rapidBookingMaxCount = 2;

  isSecEngineActive = true;
  isVpnBlockActive = true;
  isMfaRequired = false;

  success = '';

  saveSettings() {
    this.success = 'System configurations updated successfully!';
    setTimeout(() => this.success = '', 3000);
  }
}
