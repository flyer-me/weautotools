using CloudWork.Common.DB;
using CloudWork.Repository.Base;
using CloudWork.Repository.UnitOfWorks;
using CloudWork.Service;
using CloudWork.Service.Interface;
using Microsoft.EntityFrameworkCore;

namespace CloudWork.Web
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services.AddControllersWithViews();
            builder.Services.AddDbContext<CloudWorkDbContext>(options =>
            {
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection"));
                //options.UseSqlServer(builder.Configuration.GetConnectionString("WSLConnection"));
            });

            builder.Services.AddScoped<IUnitOfWork, UnitOfWork>();
            builder.Services.AddScoped(typeof(IBaseRepository<>), typeof(BaseRepository<>));
            builder.Services.AddScoped(typeof(IBaseService<>), typeof(BaseService<>));
            builder.Services.AddScoped(typeof(ITestCaseService), typeof(TestCaseService));
            builder.Services.AddScoped(typeof(IQuestionService), typeof(QuestionService));

            var app = builder.Build();

            if (!app.Environment.IsDevelopment())
            {
                app.UseExceptionHandler("/Home/Error");
            }

            app.UseStaticFiles();

            app.UseRouting();

            app.UseAuthorization();

            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Questions}/{action=Index}/{id?}");

            app.Run();
        }
    }
}
