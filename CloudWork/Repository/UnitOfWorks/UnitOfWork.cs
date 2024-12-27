using CloudWork.Data;
using CloudWork.Models;
using CloudWork.Repository.Base;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Storage;

namespace CloudWork.Repository.UnitOfWorks
{
    public class UnitOfWork : IUnitOfWork
    {
        public CloudWorkDbContext _context;
        private bool _disposed;
        private IDbContextTransaction? _transaction;

        public TestCaseRepository TestCases { get; private set; }

        public UnitOfWork(CloudWorkDbContext context)
        {
            _context = context;
            TestCases = new TestCaseRepository(_context);
        }

        public IGenericRepository<T> Repository<T>() where T : class // 不需要包含导航属性的
        {
            return new GenericRepository<T>(_context);
        }

        public void BeginTransaction()
        {
            _transaction = _context.Database.BeginTransaction();
        }

        public void CommitTransaction()
        {
            _transaction?.Commit();
        }

        public void RollbackTransaction()
        {
            _transaction?.Rollback();
            _transaction?.Dispose();
        }

        public async Task<int> SaveAsync()
        {
            return await _context.SaveChangesAsync();
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);  //
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!_disposed)
            {
                if (disposing)
                {
                    _context.Dispose();
                }
                _disposed = true;
            }
        }
    }
}
