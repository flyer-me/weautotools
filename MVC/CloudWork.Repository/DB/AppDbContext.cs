using CloudWork.Model;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using System.Reflection.Emit;

namespace CloudWork
{
    public class AppDbContext : IdentityDbContext<User>
    {
#pragma warning disable CS8618
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }
#pragma warning restore CS8618

        public override DbSet<User> Users { get; set; }
        public DbSet<Question> Questions { get; set; }
        public DbSet<Submission> Submissions { get; set; }
        public DbSet<TestCase> TestCases { get; set; }
        public DbSet<SubmissionResult> SubmissionEvaluations { get; set; }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.LogTo(Console.WriteLine, LogLevel.Information);
            // optionsBuilder.UseLazyLoadingProxies();
        }

        protected override void OnModelCreating(ModelBuilder builder)
        {
            base.OnModelCreating(builder);

            // 模型约束和关系
            builder.Entity<User>(entity =>
            {
                entity.HasIndex(u => u.UserName);
                entity.HasIndex(u => u.PhoneNumber);
                entity.Property(u => u.IsDeleted).HasDefaultValue(false);
            });

            // Role保留
            builder.Entity<IdentityUserRole<string>>()
                .HasOne<IdentityRole>()
                .WithMany()
                .HasForeignKey(ur => ur.RoleId)
                .OnDelete(DeleteBehavior.NoAction);

            builder.Entity<Question>(entity =>
            {
                entity.Property(p => p.IsPublic).HasDefaultValue(true);
                entity.HasIndex(entity => entity.Title);
                entity.HasMany(q => q.TestCases)
                      .WithOne(tc => tc.Question)
                      .HasForeignKey(tc => tc.QuestionId)
                      .OnDelete(DeleteBehavior.Restrict);
            });

            builder.Entity<Submission>(entity =>
            {
                entity.HasOne(s => s.User)
                      .WithMany(u => u.Submissions)
                      .HasForeignKey(s => s.UserId)
                      .OnDelete(DeleteBehavior.NoAction);

                entity.HasOne(s => s.Question)
                      .WithMany(p => p.Submissions)
                      .HasForeignKey(s => s.QuestionId)
                      .OnDelete(DeleteBehavior.NoAction);

                entity.HasOne(s => s.Evaluation)
                      .WithOne(se => se.Submission)
                      .HasForeignKey<SubmissionResult>(se => se.SubmissionId)
                      .OnDelete(DeleteBehavior.NoAction);
            });
        }
    }
}