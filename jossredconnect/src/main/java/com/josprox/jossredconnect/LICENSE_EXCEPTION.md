# License Exception Notice – JossRedClient

This project is distributed under the terms of the **GNU General Public License v3.0 or later (GPL-3.0-or-later)**.

However, **this license applies strictly to the codebase of the library (JossRedClient)** and **does not grant unrestricted access to the backend system known as "Joss Red"**.

---

## 🚫 Exception: Private Server Usage Restriction

The JossRedClient library interacts with a private backend system (Joss Red), which includes, but is not limited to:

- Audio/video streaming endpoints
- Authentication and session management
- Analytics and session behavior tracking
- Secure content redirection (media relay)

### ⚠ You may freely use, modify, and distribute the source code of JossRedClient under GPL-3.0, but:

> You **must request prior written authorization** to use or interact with the Joss Red backend service, regardless of project type (personal, educational, commercial, or otherwise).

Attempts to bypass access control mechanisms (e.g., token spoofing, unauthorized scraping, or DDoS) are strictly prohibited.

---

## ☑ Fair Use & Streaming Compliance

The Joss Red streaming endpoints are implemented to respect fair-use provisions and third-party content providers:

- **Live, real-time playback only** (no caching or downloading)
- **No local file persistence**
- **Temporary URIs with expiration**
- **Legal compliance with 17 U.S.C. § 107 (Fair Use)**  
- **Conformance with YouTube's Terms of Service (Section 5.B)**, which prohibit downloading or redistributing content unless explicitly authorized.

---

## 📋 ISO/IEC Compliance Statements

The infrastructure, access control, and data handling used by the Joss Red backend follow international standards for information security and data protection:

- **ISO/IEC 27001:2013** — Information Security Management Systems (ISMS)  
- **ISO/IEC 27002:2022** — Guidelines for organizational security controls  
- **ISO/IEC 27006:2007** — Requirements for auditing and certifying ISMS  
- **ISO/IEC 27701:2019** — Privacy Information Management (GDPR aligned)  
- **ISO/IEC 19770-1** — Software Asset Management (relevant to licensing and access control)

These guidelines are voluntarily adopted by the service to ensure best practices for:

- Encrypted data transmission
- Authenticated access headers
- Service isolation
- Request monitoring and threat detection
- User consent logging

---

## 🧾 Required User Disclosure

If your application integrates JossRedClient, you **must**:

1. Notify users that their requests are routed through the private **Joss Red service**.
2. Include this clause in your privacy notice:

   > _"This application does not collect personal information, except when using the Joss Red service. All such data is stored securely and may be accessed by the user at any time. You may receive push notifications related to this service. For full details, visit our Terms and Conditions on the official Joss Red website."_

3. Provide a visible link to these terms or include them within the app.
4. Notify the creator (`@josprox`) when integrating the library in a public project.

---

## ✅ GPL Scope Reminder

This exception **does not alter** the conditions of the GPLv3 license regarding code usage. It only applies to:

- The **infrastructure behind Joss Red**
- The **media endpoints and analytics services**
- **Server-side security logic**, which is not included in this codebase

---

## 📬 Contact and Access Request

To request access to the Joss Red backend or inquire about commercial/educational usage:

- Email: joss@int.josprox.com 
- Website: https://josprox.com  
- GitHub: https://github.com/josprox  

---

_Respect the code, the infrastructure, and your users. Let's build ethical and sustainable software._
