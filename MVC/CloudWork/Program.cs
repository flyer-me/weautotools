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
using System.Threading.RateLimiting;

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

            //builder.Services.AddScoped<TimerFilterAttribute>();

            builder.Services.AddDbContext<AppDbContext>(options =>
            {
                options.UseSqlite(builder.Configuration.GetConnectionString("DefaultConnection"), b => b.MigrationsAssembly("CloudWork"));
                //options.UseSqlServer(builder.Configuration.GetConnectionString("WSLConnection"), b => b.MigrationsAssembly("CloudWork"));
            });

            builder.Services.AddAuthentication()
                .AddMicrosoftAccount(options =>
                {
                    options.ClientId = builder.Configuration["OAuth:Microsoft:ClientId"] ?? string.Empty;
                    options.ClientSecret = builder.Configuration["OAuth:Microsoft:ClientSecret"] ?? string.Empty;
                })
                .AddGoogle(options =>
                {
                    options.ClientId = builder.Configuration["OAuth:Google:ClientId"] ?? string.Empty;
                    options.ClientSecret = builder.Configuration["OAuth:Google:ClientSecret"] ?? string.Empty;
                });

            builder.Services.AddIdentity<User, IdentityRole>(options =>
            {
                options.Password.RequireDigit = false;
                options.Password.RequireLowercase = false;
                options.Password.RequireNonAlphanumeric = false;
                options.Password.RequireUppercase = false;
                options.Password.RequiredLength = 3;
                options.User.RequireUniqueEmail = true;
            })
                .AddEntityFrameworkStores<AppDbContext>()
                .AddDefaultTokenProviders();

            builder.Services.AddAuthorizationBuilder()
                .AddPolicy("ChangeRole",
                policy => policy.RequireClaim("Create Role")
                                .RequireClaim("Edit Role")
                                .RequireClaim("Delete Role"));

            builder.Services.Configure<SecurityStampValidatorOptions>(o =>
                   o.ValidationInterval = TimeSpan.FromMinutes(15));

            builder.Services.ConfigureApplicationCookie(options =>
            {
                options.LoginPath = "/Account/Login";
            });

            builder.Logging.AddConsole();

            builder.Services.RegisterByServiceAttribute("CloudWork.Service");   // 注册.Service下所有服务
            builder.Services.AddScoped<IUnitOfWork, UnitOfWork>();
            builder.Services.AddScoped(typeof(IBaseRepository<>), typeof(BaseRepository<>));
            builder.Services.AddScoped(typeof(IGenericService<>), typeof(GenericService<>));

            builder.Services.AddRateLimiter(options =>
            {
                options.GlobalLimiter = PartitionedRateLimiter.Create<HttpContext, string>(httpContext =>
                    RateLimitPartition.GetFixedWindowLimiter(
                        partitionKey: httpContext.User.Identity?.Name ?? httpContext.Request.Headers.Host.ToString(),
                        factory: partition => new FixedWindowRateLimiterOptions
                        {
                            AutoReplenishment = true,
                            PermitLimit = builder.Configuration.GetValue<int>("RateLimit:PermitLimit"),
                            Window = builder.Configuration.GetValue<TimeSpan>("RateLimit:Window")
                        }));
            });

            var app = builder.Build();

            if (app.Environment.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                app.UseExceptionHandler("/Error");
                app.UseStatusCodePagesWithReExecute("/Error");
            }

            app.UseStaticFiles();
            app.UseRouting();
            app.UseRateLimiter();
            app.UseAuthentication();
            app.UseAuthorization();

            app.MapControllerRoute(
                name: "default",
                pattern: "{controller=Questions}/{action=Index}/{id?}");

            app.Run();
        }
    }
}
