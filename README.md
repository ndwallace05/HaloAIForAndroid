# MyHalo Android

MyHalo is an AI-powered productivity assistant for Android that helps you manage your tasks, notes, and schedule more efficiently. The app integrates with various services to provide a seamless productivity experience.

## Key Features

- **AI-Powered Assistant**: Get intelligent help with task management and scheduling
- **Task Management**: Create, organize, and track your tasks
- **Notes**: Take and organize notes with AI assistance
- **Calendar Integration**: Sync with Google Calendar and Microsoft Outlook
- **Email Integration**: Connect with Gmail and Outlook
- **Widget Support**: Quick access to your assistant and catch-up features
- **Screenshot Analysis**: Analyze screenshots for actionable items
- **Long-term Goals**: Track and manage your long-term objectives

## Technical Stack

- Kotlin
- Jetpack Compose
- Room Database
- Koin Dependency Injection
- Google APIs (Gmail, Calendar)
- Microsoft Graph API
- OpenAI Integration
- Azure KeyVault
- WorkManager
- MLKit

## Requirements

- Android 11 (API 30) or higher
- Google Play Services
- Internet connection

## Setup Instructions

### Prerequisites

1. Android Studio (latest version)
2. JDK 11 or higher
3. Git

### Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/MyHalo_Android.git
   ```

2. Create a `.env` file in the root directory with the following template:
   ```env
   # Google API Configuration
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret

   # Microsoft Graph API Configuration
   MS_CLIENT_ID=your_microsoft_client_id
   MS_CLIENT_SECRET=your_microsoft_client_secret
   MS_TENANT_ID=your_microsoft_tenant_id

   # OpenAI API Configuration
   OPENAI_API_KEY=your_openai_api_key

   # Azure KeyVault Configuration
   AZURE_KEYVAULT_URL=your_keyvault_url
   AZURE_CLIENT_ID=your_azure_client_id
   AZURE_CLIENT_SECRET=your_azure_client_secret
   AZURE_TENANT_ID=your_azure_tenant_id
   ```

3. Open the project in Android Studio

4. Sync the project with Gradle files

5. Build and run the app

### API Setup

1. **Google APIs**:
   - Create a project in Google Cloud Console
   - Enable Gmail and Calendar APIs
   - Create OAuth 2.0 credentials
   - Add the client ID and secret to your `.env` file

2. **Microsoft Graph API**:
   - Register an application in Azure Portal
   - Configure API permissions for Microsoft Graph
   - Add the client ID, secret, and tenant ID to your `.env` file

3. **OpenAI API**:
   - Create an account on OpenAI
   - Generate an API key
   - Add the API key to your `.env` file

4. **Azure KeyVault**:
   - Create a KeyVault in Azure Portal
   - Configure access policies
   - Add the KeyVault URL and credentials to your `.env` file

## Privacy and Security

MyHalo takes your privacy seriously. The app requires various permissions to provide its full functionality, including:
- Calendar access for scheduling
- Contact access for integration
- Internet access for cloud services
- Storage access for screenshots and files
- Notification permissions for reminders

All data is processed securely and in accordance with our privacy policy.

## Security Best Practices

1. Never commit the `.env` file to version control
2. Use environment variables for all sensitive information
3. Regularly rotate API keys and secrets
4. Keep dependencies updated to the latest secure versions
5. Use HTTPS for all API communications
6. Implement proper error handling and logging
7. Follow the principle of least privilege for API permissions

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
