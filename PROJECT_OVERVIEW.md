🌍 Project Overview — NovaPay
NovaPay is a reactive fintech microservice built with Spring Boot WebFlux + R2DBC that demonstrates how to design and deploy secure wallet transfers, savings automation, and transaction reconciliation in regulated environments.

🎯 Purpose
Deliver a scalable, event‑driven architecture for digital financial services.

Showcase compliance‑ready workflows with audit logs, reconciliation, and AML/KYC integration.

Provide a demo‑friendly project that highlights both customer and admin journeys.

Position as a global fintech solution adaptable to cross‑border payments and savings automation.

👤 Customer Journey
Onboarding & Authentication → Sign up, complete KYC, log in securely with JWT/MFA.

Wallet Funding → Link bank or mobile money service (e.g., M‑Pesa, PayPal, Stripe).

Balance Check → View wallet balance in real time via reactive APIs.

Initiate Transfer → Enter recipient wallet ID, amount, and purpose.

System validates balance, transaction limits, and AML rules.

Execution → Debit sender wallet, credit recipient wallet, update immutable ledger.

Notification → Receive instant confirmation (push/email).

Savings Automation (Optional) → Set recurring transfers to a savings wallet with goal tracking.

🛠️ Admin Journey
Reconciliation Service → Runs daily to match transfers against ledger entries.

Audit Logs → Capture every action for compliance reporting.

Monitoring Dashboards → Track system health, fraud alerts, and operational reliability.