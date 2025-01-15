using CloudWork.Common.Extensions;
using CloudWork.Filter;
using CloudWork.Model;
using CloudWork.Repository.Base;
using CloudWork.Repository.UnitOfWork;
using CloudWork.Service;
using CloudWork.Service.Interface;
using Microsoft.AspNetCore.Http.Features;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CloudWork
{
    public class Program
    {
        public static void Main(string[] args)
        {
            var builder = WebApplication.CreateBuilder(args);

            builder.Services.AddControllersWithViews(options =>
            {
                options.CacheProfiles.Add("Default", new CacheProfile
                {
                    Duration = 120,
                    Location = ResponseCacheLocation.Any,
                    VaryByHeader = "User-Agent, Accept-Language",
                    NoStore = false
                });

                options.Filters.Add<TimerFilterAttribute>();
            });

            builder.Services.Configure<FormOptions>(options =>
                {
                    options.MultipartBodyLengthLimit = 10_000_000; // 10MB
                });

            builder.Services.AddScoped<TimerFilterAttribute>();

            builder.Services.AddDbContext<AppDbContext>(options =>
            {
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection"), b => b.MigrationsAssembly("CloudWork"));
                //options.UseSqlServer(builder.Configuration.GetConnectionString("WSLConnection"), b => b.MigrationsAssembly("CloudWork"));
            });

            builder.Services.AddIdentity<User, IdentityRole>(options => {
                options.Password.RequireDigit = false;
                options.Password.RequireLowercase = false;
                options.Password.RequireNonAlphanumeric = false;
                options.Password.RequireUppercase = false;
                options.Password.RequiredLength = 3;
            })
                .AddEntityFrameworkStores<AppDbContext>()
                .AddDefaultTokenProviders();

            builder.Services.ConfigureApplicationCookie(options =>
            {
                options.LoginPath = "/Account/Login";
            });

            builder.Logging.AddConsole();

            builder.Services.RegisterByServiceAttribute("CloudWork.Service");
            builder.Services.AddScoped<IUnitOfWork, UnitOfWork>();
            builder.Services.AddScoped(typeof(IBaseRepository<>), typeof(BaseRepository<>));
            builder.Services.AddScoped(typeof(IGenericService<>), typeof(GenericService<>));

            var app = builder.Build();

            if (!app.Environment.IsDevelopment())
            {
                app.UseExceptionHandler("/Home/Error");
            }
            else
            {
                app.UseDeveloperExceptionPage();
            }

            app.UseStaticFiles();

            app.UseRouting();

            app.UseAuthentication();
            app.UseAuthorization();

            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Questions}/{action=Index}/{id?}");

            app.Run();
        }
    }
}
