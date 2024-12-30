using CloudWork.Common.DB;
using CloudWork.Common.Extensions;
using CloudWork.Repository.Base;
using CloudWork.Repository.UnitOfWork;
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
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection"),
                    b => b.MigrationsAssembly("CloudWork"));
                //options.UseSqlServer(builder.Configuration.GetConnectionString("WSLConnection"));
            });

            builder.Logging.AddConsole();

            builder.Services.RegisterByServiceAttribute();
            builder.Services.AddScoped<IUnitOfWork, UnitOfWork>();
            builder.Services.AddScoped(typeof(IBaseRepository<>), typeof(BaseRepository<>));
            builder.Services.AddScoped(typeof(IGenericService<>), typeof(GenericService<>));

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
