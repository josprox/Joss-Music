# Terms and Conditions of Use – Joss Red Service

**Last updated:** May 6, 2025  
**Author:** José Luis Melchor Estrada (https://josprox.com)  
**Contact:** joss@int.josprox.com

---

## Introduction

The library (`JossRedClient`) is licensed under the **GNU General Public License v3.0 or later** (GPL-3.0-or-later). However, all connections made to the **Joss Red** backend infrastructure—including streaming services, login systems, and private endpoints—are subject to the following additional terms of use.

---

## 1. Use of the Joss Red Service

Any application, system, or project that uses this library to interact with Joss Red services must:

- **Request prior permission** from the author before accessing any Joss Red functionality.
- **Inform the end-user** that the application relies on a private service provided by Joss Red.
- Clearly disclose in the privacy policy that while the app may not collect personal data, **Joss Red services may generate and store analytical data** (e.g., access logs, usage patterns).
- **Invite or credit the author**, José Luis Melchor Estrada, in the project where the library is used (as a contributor or with visible attribution).

---

## 2. Data Collection and Transparency

You must include a privacy clause similar to the following in your application:

> _"This application does not collect personal user data, except when the Joss Red service is used. Data processed by this service is stored in a private database and may be available for download by the user upon request. Additionally, installing this application may result in receiving push notifications related to our services, including Joss Red. For more information, please refer to our official website at https://josprox.com.”_

---

## 3. Analytics and Data Storage

Joss Red may collect non-personal usage information, including:

- Public IP address
- Date and time of request
- Media identifier
- Authentication via `X-JossRed-Auth` header
- Platform and client type

These records are used solely for monitoring and improving the service, and **will never be sold or shared with third parties**.

---

## 4. Source of Streams and Legal Use

All **media streams and API endpoints originate from Joss Red servers**, which serve as intermediaries between the client and trusted public services such as YouTube.

Joss Red **strictly adheres to YouTube’s Terms of Service**, particularly:

> **YouTube Terms of Service – Section 5.B:**  
> "You shall not download any Content unless you see a 'download' or similar link displayed by YouTube on the Service for that Content."

Joss Red complies with this by providing **stream-only access** (e.g., radio-style playback), and **explicitly disables downloading functionality**. This practice is recognized under **Fair Use (17 U.S. Code § 107)** in the United States, and under similar clauses in international copyright laws, **as long as:**

- No permanent copies of the content are stored or made downloadable.
- The content is not redistributed for profit.
- The content is accessed publicly as intended by the platform.

Use of Joss Red’s streaming as a radio-style experience is **legal under “stream forwarding”** when no part of the data is saved, captured, or monetized without rights.

---

## 5. Derived Projects

If your project uses this library (`JossRedClient`) to interact with the Joss Red API or services, you must:

- Disclose the usage of Joss Red to your users.
- Attribute or invite the original author.
- Respect all related terms of service for upstream providers (e.g., YouTube, etc.).

---

## 6. Disclaimer

Unauthorized or abusive use of the Joss Red service—especially without explicit permission—may result in:

- Revocation of access
- IP blocking or token bans
- Legal action in case of license or copyright violations

---

## 7. Contact

To request usage permission, submit questions, or report a concern, please contact:

📧 joss@int.josprox.com  
🌐 https://josprox.com

---

**Thank you for respecting and supporting responsible open-source development.**
