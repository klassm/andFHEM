#!/bin/bash
sed -i 's/\(private ProviderType billingProvider = ProviderType\.\)GOOGLE/\1AMAZON/g' amazon/src/li/klass/fhem/billing/BillingService.java
