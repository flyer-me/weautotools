using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CloudWork.Service.Interface
{
    public interface IEmailSenderService
    {
        Task SendConfirmationEmailAsync(string toEmail, string? userName, string safeLink);
        Task SendEmailAsync(string toEmail, string subject, string htmlMessage);
    }
}
