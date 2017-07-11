using System;
using System.Net.Http;
using System.Windows;
using Microsoft.AspNet.SignalR.Client;

namespace WPFClient
{
    /// <summary>
    /// SignalR client hosted in a WPF application. The client
    /// lets the user pick a user name, connect to the server asynchronously
    /// to not block the UI thread, and send chat messages to all connected 
    /// clients whether they are hosted in WinForms, WPF, or a web application.
    /// For simplicity, MVVM will not be used for this sample.
    /// </summary>
    public partial class MainWindow : Window
    {
        /// <summary>
        /// This name is simply added to sent messages to identify the user; this 
        /// sample does not include authentication.
        /// </summary>
        public string UserName { get; set; }
        public IHubProxy HubProxy { get; set; }
        const string ServerURI = "http://192.168.0.106:27/signalr";
        private HubConnection Connection { get; set; }

        public MainWindow()
        {
            InitializeComponent();
        }

        private void ButtonSend_Click(object sender, RoutedEventArgs e)
        {
            HubProxy.Invoke("Send", UserName, TextBoxMessage.Text);
            TextBoxMessage.Text = string.Empty;
            TextBoxMessage.Focus();
        }

        /// <summary> 
        /// Creates and connects the hub connection and hub proxy. This method 
        /// is called asynchronously from SignInButton_Click. 
        /// </summary> 
        private async void ConnectAsync()
        {
            Connection = new HubConnection(ServerURI);
            Connection.Closed += Connection_Closed;
            HubProxy = Connection.CreateHubProxy("MyHub");
            //Handle incoming event from server: use Invoke to write to console from SignalR's thread 
            HubProxy.On<string, string>("AddMessage", (name, message) =>
                Dispatcher.Invoke(() =>
                    RichTextBoxConsole.AppendText(string.Format("{0}: {1}\r", name, message))
                )
            );
            try
            {
                await Connection.Start();
            }
            catch (HttpRequestException)
            {
                StatusText.Content = "Unable to connect to server: Start server before connecting clients.";
                //No connection: Don't enable Send button or show chat UI 
                return;
            }

            //Show chat UI; hide login UI 
            SignInPanel.Visibility = Visibility.Collapsed;
            ChatPanel.Visibility = Visibility.Visible;
            ButtonSend.IsEnabled = true;
            TextBoxMessage.Focus();
            RichTextBoxConsole.AppendText("Connected to server at " + ServerURI + "\r");
        }

        /// <summary> 
        /// If the server is stopped, the connection will time out after 30 seconds (default), and the  
        /// Closed event will fire. 
        /// </summary> 
        void Connection_Closed()
        {
            try {
                //Hide chat UI; show login UI 
                System.Windows.Threading.Dispatcher dispatcher = Application.Current.Dispatcher;
                dispatcher.Invoke(() => ChatPanel.Visibility = Visibility.Collapsed);
                dispatcher.Invoke(() => ButtonSend.IsEnabled = false);
                dispatcher.Invoke(() => StatusText.Content = "You have been disconnected.");
                dispatcher.Invoke(() => SignInPanel.Visibility = Visibility.Visible);
            } catch (Exception e) { StatusText.Content = e.Message + "\n\n\n" + e.StackTrace; }
        }

        private void SignInButton_Click(object sender, RoutedEventArgs e)
        {
            UserName = UserNameTextBox.Text;
            //Connect to server (use async method to avoid blocking UI thread) 
            try
            {
                if (!string.IsNullOrEmpty(UserName))
                {
                    StatusText.Visibility = Visibility.Visible;
                    StatusText.Content = "Connecting to server...";
                    ConnectAsync();
                }
            }
            catch (Exception ex)
            {
                StatusText.Visibility = Visibility.Visible;
                StatusText.Content = ex.Message;
            }
        }

        private void WPFClient_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            if (Connection != null)
            {
                Connection.Stop();
                Connection.Dispose();
            }
        }
    }
}
