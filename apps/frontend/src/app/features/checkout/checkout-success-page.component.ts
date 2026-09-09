import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CheckoutProcessStore } from './checkout-process.store';

@Component({
  selector: 'app-checkout-success-page',
  imports: [CurrencyPipe, DatePipe, RouterLink],
  templateUrl: './checkout-success-page.component.html',
  styleUrl: './checkout-success-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CheckoutSuccessPageComponent {
  readonly checkoutProcess = inject(CheckoutProcessStore);

  startAnotherPurchase(): void {
    this.checkoutProcess.reset();
  }
}
