using System;
using System.Reflection;
using System.Threading.Tasks;
using System.Windows.Forms;
using Microsoft.AspNet.SignalR;
using Microsoft.Owin.Cors;
using Microsoft.Owin.Hosting;
using NetFwTypeLib;
using Owin;

namespace SignalRChat
{
    /// INBOUND & OUTBOUND RULE hisselerinde 27 portunu allow et Windows Firewallda

    /// <summary>
    /// WinForms host for a SignalR server. The host can stop and start the SignalR
    /// server, report errors when trying to start the server on a URI where a
    /// server is already being hosted, and monitor when clients connect and disconnect. 
    /// The hub used in this server is a simple echo service, and has the same 
    /// functionality as the other hubs in the SignalR Getting Started tutorials.
    /// </summary>
    public partial class WinFormsServer : Form
    {
        private IDisposable SignalR { get; set; }
        private string serverURI = "http://192.168.0.106:27/"; // local IPv4 Address of the server (computer)

        internal WinFormsServer()
        {
            InitializeComponent();

            CreateFirewallRule();
        }

        /// <summary>
        /// Removes a firewall rule (if exists) then adds new rule.
        /// Rule name: SignalR_PORT
        /// </summary>
        private void CreateFirewallRule()
        {
            INetFwPolicy2 firewallPolicy = (INetFwPolicy2)Activator.CreateInstance(Type.GetTypeFromProgID("HNetCfg.FwPolicy2"));
            try
            {
                firewallPolicy.Rules.Remove("SignalR_PORT");
            }
            finally
            {
                INetFwRule firewallRule = (INetFwRule)Activator.CreateInstance(Type.GetTypeFromProgID("HNetCfg.FWRule"));
                firewallRule.Action = NET_FW_ACTION_.NET_FW_ACTION_ALLOW;
                firewallRule.Protocol = (int)NET_FW_IP_PROTOCOL_.NET_FW_IP_PROTOCOL_TCP;
                firewallRule.LocalPorts = "27";
                //firewallRule.RemotePorts = "27";
                firewallRule.Description = "Used to allow port #27 for SignalR.";
                firewallRule.Direction = NET_FW_RULE_DIRECTION_.NET_FW_RULE_DIR_IN;
                firewallRule.Enabled = true;
                firewallRule.InterfaceTypes = "All";
                firewallRule.Name = "SignalR_PORT";
                firewallPolicy.Rules.Add(firewallRule);

                INetFwRule firewallRule1 = (INetFwRule)Activator.CreateInstance(Type.GetTypeFromProgID("HNetCfg.FWRule"));
                firewallRule1.Action = NET_FW_ACTION_.NET_FW_ACTION_ALLOW;
                firewallRule1.Protocol = (int)NET_FW_IP_PROTOCOL_.NET_FW_IP_PROTOCOL_TCP;
                firewallRule1.LocalPorts = "27";
                //firewallRule1.RemotePorts = "27";
                firewallRule1.Description = "Used to allow port #27 for SignalR.";
                firewallRule1.Direction = NET_FW_RULE_DIRECTION_.NET_FW_RULE_DIR_OUT;
                firewallRule1.Enabled = true;
                firewallRule1.InterfaceTypes = "All";
                firewallRule1.Name = "SignalR_PORT";
                firewallPolicy.Rules.Add(firewallRule1);
            }
        }

        /// <summary>
        /// Calls the StartServer method with Task.Run to not
        /// block the UI thread. 
        /// </summary>
        private void ButtonStart_Click(object sender, EventArgs e)
        {
            WriteToConsole("Starting server...");
            ButtonStart.Enabled = false;
            Task.Run(() => StartServer());
        }

        /// <summary>
        /// Stops the server and closes the form. Restart functionality omitted
        /// for clarity.
        /// </summary>
        private void ButtonStop_Click(object sender, EventArgs e)
        {
            //SignalR will be disposed in the FormClosing event
            Close();
        }

        /// <summary>
        /// Starts the server and checks for error thrown when another server is already 
        /// running. This method is called asynchronously from Button_Start.
        /// </summary>
        private void StartServer()
        {
            try
            {
                SignalR = WebApp.Start(serverURI);
            }
            catch (TargetInvocationException e)
            {
                WriteToConsole("Server failed to start. A server is already running on " + serverURI);
                //Re-enable button to let user try to start server again
                Invoke((Action)(() => ButtonStart.Enabled = true));
                WriteToConsole("\n\nerror: " + e.Message);
                return;
            }
            Invoke((Action)(() => ButtonStop.Enabled = true));
            WriteToConsole("Server started at " + serverURI);
        }

        /// <summary>
        /// This method adds a line to the RichTextBoxConsole control, using Invoke if used
        /// from a SignalR hub thread rather than the UI thread.
        /// </summary>
        /// <param name="message"></param>
        internal void WriteToConsole(string message)
        {
            if (RichTextBoxConsole.InvokeRequired)
            {
                Invoke((Action)(() =>
                    WriteToConsole(message)
                ));
                return;
            }
            RichTextBoxConsole.AppendText(message + Environment.NewLine);
        }

        private void WinFormsServer_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (SignalR != null)
                SignalR.Dispose();
        }
    }
    
    // 45.35.4.29
    /// <summary>
    /// Used by OWIN's startup process. 
    /// </summary>
    class Startup
    {
        public void Configuration(IAppBuilder app)
        {
            app.UseCors(CorsOptions.AllowAll);
            app.MapSignalR();
        }
    }

    /// <summary>
    /// Echoes messages sent using the Send message by calling the
    /// addMessage method on the client. Also reports to the console
    /// when clients connect and disconnect.
    /// </summary>
    public class ChatHub : Hub
    {
        public void Send(string name, string message)
        {
            string[] darks = new string[] { "wpf1", "wpf2" };
            Clients.Others.AddMessage(name, message); // works!
        }
        public override Task OnConnected()
        {
            Program.MainForm.WriteToConsole("Client connected: " + Context.ConnectionId);
            return base.OnConnected();
        }
        public override Task OnDisconnected(bool stopCalled)
        {
            Program.MainForm.WriteToConsole("Client disconnected: " + Context.ConnectionId); 
            return base.OnDisconnected(stopCalled);
        }
        public override Task OnReconnected()
        {
            return base.OnReconnected();
        }
    }
}
